package np.org.nrb.pars.ai.ingestion;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Heading- and page-aware chunker. Token counts are estimated as characters/4. */
@Service
public class ChunkingService {

    private static final Pattern PAGE_MARKER = Pattern.compile("\\[\\[PAGE (\\d+)\\]\\]");
    private final ParsAiProperties properties;

    public ChunkingService(ParsAiProperties properties) {
        this.properties = properties;
    }

    public record Chunk(String text, String section, Integer page, int startOffset, int endOffset) {}

    private record Segment(String text, Integer page) {}

    public List<Chunk> chunk(String documentText) {
        int maxChars = properties.getChunkSizeTokens() * 4;
        int overlapChars = properties.getChunkOverlapTokens() * 4;
        List<Chunk> chunks = new ArrayList<>();
        for (Segment segment : segments(documentText)) {
            StringBuilder section = new StringBuilder();
            StringBuilder buffer = new StringBuilder();
            int chunkStart = 0;
            for (String paragraph : segment.text().split("\\n\\s*\\n")) {
                String clean = paragraph.trim();
                if (clean.isEmpty()) {
                    continue;
                }
                // A single paragraph (e.g. spreadsheet text) can exceed the model context.
                // Hard-split it so no single document sent for embedding is oversized.
                if (clean.length() > maxChars) {
                    int from = 0;
                    while (from < clean.length()) {
                        int to = Math.min(clean.length(), from + maxChars);
                        chunks.add(new Chunk(clean.substring(from, to), section.toString(),
                                segment.page(), from, to));
                        from = to - Math.min(overlapChars, to - from - 1);
                    }
                    continue;
                }
                if (clean.length() < 100 && (clean.matches("^\\d+(\\.\\d+)*\\s+.*") || clean.equals(clean.toUpperCase()))) {
                    section.setLength(0);
                    section.append(clean);
                }
                if (buffer.length() + clean.length() + 2 > maxChars && buffer.length() > 0) {
                    String text = buffer.toString().trim();
                    chunks.add(new Chunk(text, section.toString(), segment.page(), chunkStart, chunkStart + text.length()));
                    String overlap = text.length() > overlapChars ? text.substring(text.length() - overlapChars) : "";
                    buffer = new StringBuilder(overlap);
                    chunkStart = chunkStart + text.length() - overlap.length();
                }
                if (buffer.length() > 0) {
                    buffer.append("\n\n");
                }
                buffer.append(clean);
            }
            if (buffer.length() > 0) {
                String text = buffer.toString().trim();
                chunks.add(new Chunk(text, section.toString(), segment.page(), chunkStart, chunkStart + text.length()));
            }
        }
        return chunks;
    }

    private List<Segment> segments(String text) {
        List<Segment> segments = new ArrayList<>();
        Matcher matcher = PAGE_MARKER.matcher(text);
        int cursor = 0;
        Integer currentPage = null;
        while (matcher.find()) {
            if (cursor < matcher.start()) {
                String before = text.substring(cursor, matcher.start()).trim();
                if (!before.isEmpty()) {
                    segments.add(new Segment(before, currentPage));
                }
            }
            currentPage = Integer.parseInt(matcher.group(1));
            cursor = matcher.end();
        }
        if (cursor < text.length()) {
            String remaining = text.substring(cursor).trim();
            if (!remaining.isEmpty()) {
                segments.add(new Segment(remaining, currentPage));
            }
        }
        return segments;
    }
}
