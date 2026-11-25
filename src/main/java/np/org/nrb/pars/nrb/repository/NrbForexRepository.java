package np.org.nrb.pars.nrb.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import np.org.nrb.pars.nrb.domain.NrbForexDtos;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexDayView;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexRateView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Persistent audit/history of official NRB forex rates. Idempotent by (rate_date, content_hash). */
@Repository
public class NrbForexRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public NrbForexRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public int saveDay(String sourceUrl, NrbForexDtos.NrbForexDay day) {
        String raw;
        try {
            raw = mapper.writeValueAsString(day);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize NRB forex payload", e);
        }
        String hash = sha256(raw);
        UUID id = jdbc.query("""
                INSERT INTO ai.nrb_forex_snapshots
                 (id, source_url, rate_date, published_on, modified_on, content_hash, raw_payload)
                VALUES (?, ?, ?::date, ?, ?, ?, ?::jsonb)
                ON CONFLICT (rate_date, content_hash) DO NOTHING
                RETURNING id
                """, rs -> rs.next() ? UUID.fromString(rs.getString(1)) : null,
                UUID.randomUUID(), sourceUrl, day.date(), day.publishedOn(), day.modifiedOn(), hash, raw);
        if (id == null) {
            return 0; // unchanged content — no duplicate indexing
        }
        for (NrbForexDtos.NrbForexRate rate : day.safeRates()) {
            jdbc.update("""
                    INSERT INTO ai.nrb_forex_rates
                     (id, snapshot_id, iso3, name, unit, buy, sell, raw_buy, raw_sell)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, UUID.randomUUID(), id, rate.iso3(), rate.name(), rate.unit(),
                    rate.buyAmount(), rate.sellAmount(), rate.buy(), rate.sell());
        }
        return 1;
    }

    public List<ForexDayView> findRange(LocalDate from, LocalDate to) {
        List<ForexDayView> days = jdbc.query("""
                SELECT s.id AS snapshot_id, s.rate_date, s.published_on, s.modified_on,
                       s.fetched_at, s.source_url,
                       r.iso3, r.name, r.unit, r.buy, r.sell, r.raw_buy, r.raw_sell
                FROM ai.nrb_forex_snapshots s
                JOIN ai.nrb_forex_rates r ON r.snapshot_id = s.id
                WHERE s.rate_date BETWEEN ? AND ?
                ORDER BY s.rate_date DESC, r.iso3
                """, (rs, row) -> mapRow(rs), from, to);
        return groupDays(days);
    }

    public Optional<ForexDayView> findLatest() {
        List<ForexDayView> rows = jdbc.query("""
                SELECT s.id AS snapshot_id, s.rate_date, s.published_on, s.modified_on,
                       s.fetched_at, s.source_url,
                       r.iso3, r.name, r.unit, r.buy, r.sell, r.raw_buy, r.raw_sell
                FROM ai.nrb_forex_snapshots s
                JOIN ai.nrb_forex_rates r ON r.snapshot_id = s.id
                WHERE s.rate_date = (SELECT MAX(rate_date) FROM ai.nrb_forex_snapshots)
                ORDER BY r.iso3
                """, (rs, row) -> mapRow(rs));
        List<ForexDayView> days = groupDays(rows);
        return days.stream().findFirst();
    }

    public Optional<ForexDayView> findByCurrencyAndDate(String iso3, LocalDate date) {
        List<ForexDayView> rows = jdbc.query("""
                SELECT s.id AS snapshot_id, s.rate_date, s.published_on, s.modified_on,
                       s.fetched_at, s.source_url,
                       r.iso3, r.name, r.unit, r.buy, r.sell, r.raw_buy, r.raw_sell
                FROM ai.nrb_forex_snapshots s
                JOIN ai.nrb_forex_rates r ON r.snapshot_id = s.id
                WHERE s.rate_date = ? AND UPPER(r.iso3) = UPPER(?)
                ORDER BY s.rate_date DESC
                """, (rs, row) -> mapRow(rs), date, iso3);
        List<ForexDayView> days = groupDays(rows);
        return days.stream().findFirst();
    }

    private ForexDayView mapRow(ResultSet rs) throws SQLException {
        ForexRateView rate = new ForexRateView(
                rs.getString("iso3"), rs.getString("name"), rs.getInt("unit"),
                normalize(rs.getBigDecimal("buy")), normalize(rs.getBigDecimal("sell")),
                rs.getString("raw_buy"), rs.getString("raw_sell"));
        var ts = rs.getTimestamp("fetched_at");
        return new ForexDayView(
                rs.getDate("rate_date").toLocalDate(),
                rs.getString("published_on"), rs.getString("modified_on"),
                ts == null ? null : ts.toInstant(), rs.getString("source_url"), List.of(rate));
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return null;
        }
        int scale = Math.max(2, value.stripTrailingZeros().scale());
        return value.setScale(scale);
    }

    private List<ForexDayView> groupDays(List<ForexDayView> flatRows) {
        Map<LocalDate, List<ForexRateView>> grouped = new LinkedHashMap<>();
        Map<LocalDate, ForexDayView> meta = new LinkedHashMap<>();
        for (ForexDayView row : flatRows) {
            grouped.computeIfAbsent(row.date(), k -> new ArrayList<>()).addAll(row.rates());
            meta.putIfAbsent(row.date(), row);
        }
        List<ForexDayView> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ForexRateView>> entry : grouped.entrySet()) {
            ForexDayView m = meta.get(entry.getKey());
            result.add(new ForexDayView(m.date(), m.publishedOn(), m.modifiedOn(),
                    m.fetchedAt(), m.sourceUrl(), entry.getValue()));
        }
        return result;
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash NRB payload", e);
        }
    }
}
