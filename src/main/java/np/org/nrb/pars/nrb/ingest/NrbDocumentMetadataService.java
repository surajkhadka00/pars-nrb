package np.org.nrb.pars.nrb.ingest;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts official publication metadata from source HTML and text. Never translates. */
@Component
public class NrbDocumentMetadataService {

    private static final Pattern DOC_NUMBER = Pattern.compile(
            "(?:Ref\\.?|No\\.?|Letter No\\.?|Circular No\\.?)\\s*[:\\-]?\\s*([A-Za-z0-9/\\-_.]{4,60})",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern FISCAL_YEAR = Pattern.compile("(20\\d{2})[/\\-](20\\d{2})");

    public record Metadata(String title, String documentNumber, String department,
                           String fiscalYear, String language, LocalDate publicationDate) {}

    public Metadata extract(byte[] bytes, String extension, String fallbackTitle, String text) {
        String title = fallbackTitle;
        if ("html".equalsIgnoreCase(extension)) {
            org.jsoup.nodes.Document doc = Jsoup.parse(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
            String htmlTitle = doc.title();
            if (htmlTitle != null && !htmlTitle.isBlank()) {
                title = htmlTitle.trim();
            }
        }
        String documentNumber = firstGroup(DOC_NUMBER, text);
        String fiscalYear = firstGroup(FISCAL_YEAR, text);
        String language = containsDevanagari(text) ? "NE" : "EN";
        return new Metadata(title, documentNumber, null, fiscalYear, language, parseDateFromText(text));
    }

    private String firstGroup(Pattern pattern, String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean containsDevanagari(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u0900' && c <= '\u097F') {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseDateFromText(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = Pattern.compile("\\b(20\\d{2})-(\\d{2})-(\\d{2})\\b").matcher(text);
        if (m.find()) {
            try {
                return LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)));
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }
}
