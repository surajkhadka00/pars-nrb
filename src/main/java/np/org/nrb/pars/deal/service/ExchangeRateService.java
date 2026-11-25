package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.deal.model.Currency;
import np.org.nrb.pars.deal.model.ExchangeRate;
import np.org.nrb.pars.deal.model.dto.ExchangeRateImportResponse;
import np.org.nrb.pars.deal.repository.CurrencyRepository;
import np.org.nrb.pars.deal.repository.ExchangeRateRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExchangeRateService {

    private static final String NPR = "NPR";
    private static final int HEADER_SCAN_ROWS = 8;

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, CurrencyRepository currencyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public ExchangeRateImportResponse importExcel(MultipartFile file, LocalDate rateDate) {
        if (rateDate == null) {
            throw Exceptions.badRequest("rateDate is required");
        }
        if (file == null || file.isEmpty()) {
            throw Exceptions.badRequest("Excel file is required");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            throw Exceptions.badRequest("File must be an .xlsx or .xls workbook");
        }

        Currency npr = currencyRepository.findByIsoCodeIgnoreCase(NPR)
                .orElseThrow(() -> Exceptions.notFound("NPR currency is not configured"));

        List<ParsedRate> rows = readRows(file);
        if (rows.isEmpty()) {
            throw Exceptions.badRequest("Excel file has no exchange rate rows");
        }

        Map<String, ParsedRate> byCurrency = new LinkedHashMap<>();
        List<String> unknown = new ArrayList<>();
        for (ParsedRate row : rows) {
            if (NPR.equalsIgnoreCase(row.currencyName()) || "nepalese rupee".equalsIgnoreCase(row.currencyName())) {
                throw Exceptions.badRequest("Row " + row.excelRow() + ": NPR cannot be imported as a foreign currency");
            }
            Optional<Currency> currency = findCurrency(row.currencyName());
            if (currency.isEmpty()) {
                unknown.add(row.currencyName());
                continue;
            }
            String key = currency.get().getId().toString();
            if (byCurrency.containsKey(key)) {
                throw Exceptions.badRequest("Currency " + row.currencyName() + " appears more than once");
            }
            byCurrency.put(key, row.withCurrency(currency.get()));
        }
        if (!unknown.isEmpty()) {
            throw Exceptions.badRequest("Currency not found: " + String.join(", ", unknown));
        }

        int imported = 0;
        for (ParsedRate row : byCurrency.values()) {
            ExchangeRate rate = exchangeRateRepository
                    .findByBaseCurrencyIdAndCurrencyIdAndRateDate(npr.getId(), row.currency().getId(), rateDate)
                    .orElseGet(ExchangeRate::new);
            rate.setBaseCurrency(npr);
            rate.setCurrency(row.currency());
            rate.setUnit(row.unit());
            rate.setRate(row.rate());
            rate.setRateDate(rateDate);
            exchangeRateRepository.save(rate);
            imported++;
        }
        return new ExchangeRateImportResponse(rateDate, imported);
    }

    @Transactional
    public void delete(UUID id) {
        ExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> Exceptions.notFound("Exchange rate not found"));
        exchangeRateRepository.delete(rate);
    }

    private List<ParsedRate> readRows(MultipartFile file) {
        Workbook workbook = openWorkbook(file);
        try (workbook) {
            if (workbook.getNumberOfSheets() == 0) {
                throw Exceptions.badRequest("Excel file has no sheets");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Header header = findHeader(sheet, formatter);
            List<ParsedRate> rows = new ArrayList<>();
            for (int index = header.lastHeaderRow() + 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null) {
                    continue;
                }
                String currencyName = cellText(row, header.currencyColumn(), formatter);
                if (currencyName.isBlank() || "currency".equalsIgnoreCase(currencyName)) {
                    continue;
                }
                int excelRow = row.getRowNum() + 1;
                int unit = parseUnit(cellText(row, header.unitColumn(), formatter), excelRow);
                BigDecimal rate = parseRate(cellText(row, header.buyingColumn(), formatter), excelRow);
                rows.add(new ParsedRate(excelRow, unit, currencyName, rate, null));
            }
            return rows;
        } catch (IOException ex) {
            throw Exceptions.badRequest("File is not a valid Excel workbook");
        }
    }

    private static Workbook openWorkbook(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return WorkbookFactory.create(input);
        } catch (IOException | IllegalArgumentException ex) {
            throw Exceptions.badRequest("File is not a valid Excel workbook");
        }
    }

    private Header findHeader(Sheet sheet, DataFormatter formatter) {
        Map<String, Integer> columns = new HashMap<>();
        int lastHeaderRow = -1;
        int lastScan = Math.min(HEADER_SCAN_ROWS, sheet.getLastRowNum());
        for (int index = 0; index <= lastScan; index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }
            for (Cell cell : row) {
                String label = cellText(row, cell.getColumnIndex(), formatter).toUpperCase(Locale.ROOT);
                if (label.equals("UNIT") || label.equals("CURRENCY") || label.equals("MIDDLE") || label.equals("BUYING")) {
                    columns.put(label, cell.getColumnIndex());
                    lastHeaderRow = index;
                }
            }
        }
        if (!columns.containsKey("CURRENCY") || !columns.containsKey("BUYING")) {
            throw Exceptions.badRequest("Excel header must include CURRENCY and BUYING columns");
        }
        int unitColumn = columns.getOrDefault("UNIT", 0);
        return new Header(lastHeaderRow, unitColumn, columns.get("CURRENCY"), columns.get("BUYING"));
    }

    private Optional<Currency> findCurrency(String name) {
        String trimmed = name.trim();
        if (trimmed.length() == 3) {
            Optional<Currency> byCode = currencyRepository.findByIsoCodeIgnoreCase(trimmed);
            if (byCode.isPresent()) {
                return byCode;
            }
        }
        return currencyRepository.findByDescriptionIgnoreCase(trimmed);
    }

    private static int parseUnit(String text, int excelRow) {
        if (text == null || text.isBlank()) {
            return 1;
        }
        try {
            BigDecimal value = new BigDecimal(text.replace(",", "").trim());
            if (value.stripTrailingZeros().scale() > 0 || value.intValueExact() <= 0) {
                throw Exceptions.badRequest("Row " + excelRow + ": unit must be a positive whole number");
            }
            return value.intValueExact();
        } catch (ArithmeticException | NumberFormatException ex) {
            throw Exceptions.badRequest("Row " + excelRow + ": unit must be a positive whole number");
        }
    }

    private static BigDecimal parseRate(String text, int excelRow) {
        if (text == null || text.isBlank()) {
            throw Exceptions.badRequest("Row " + excelRow + ": buying rate is required");
        }
        try {
            BigDecimal rate = new BigDecimal(text.replace(",", "").trim());
            if (rate.signum() <= 0) {
                throw Exceptions.badRequest("Row " + excelRow + ": buying rate must be greater than 0");
            }
            return rate;
        } catch (NumberFormatException ex) {
            throw Exceptions.badRequest("Row " + excelRow + ": buying rate must be a number");
        }
    }

    private static String cellText(Row row, int column, DataFormatter formatter) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private record Header(int lastHeaderRow, int unitColumn, int currencyColumn, int buyingColumn) {
    }

    private record ParsedRate(int excelRow, int unit, String currencyName, BigDecimal rate, Currency currency) {
        private ParsedRate withCurrency(Currency resolved) {
            return new ParsedRate(excelRow, unit, currencyName, rate, resolved);
        }
    }
}
