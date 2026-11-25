package np.org.nrb.pars.ai.ingestion;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Extracts text with page information for PDFs and generic text for Office/HTML/TXT. */
@Service
public class DocumentExtractionService {

    public record Extracted(String text, boolean hasText) {}

    private final Tika tika = new Tika();

    public Extracted extract(byte[] bytes, String extension) {
        try {
            if ("pdf".equals(extension)) {
                return extractPdf(bytes);
            }
            String text = tika.parseToString(new ByteArrayInputStream(bytes));
            return new Extracted(text == null ? "" : text.trim(), text != null && !text.isBlank());
        } catch (Exception e) {
            throw new IllegalStateException("Document extraction failed: " + e.getMessage(), e);
        }
    }

    private Extracted extractPdf(byte[] bytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                throw new IllegalStateException("Encrypted PDFs are not supported");
            }
            StringBuilder all = new StringBuilder();
            int pages = document.getNumberOfPages();
            for (int page = 1; page <= pages; page++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document);
                if (text != null && !text.isBlank()) {
                    all.append("[[PAGE ").append(page).append("]]\n").append(text).append('\n');
                }
            }
            String result = all.toString().trim();
            return new Extracted(result, !result.isBlank());
        }
    }

    public String readTextFile(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Kept for API symmetry with InputStream-based readers used by Spring AI readers. */
    public InputStreamReader asReader(byte[] bytes) {
        return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
    }
}
