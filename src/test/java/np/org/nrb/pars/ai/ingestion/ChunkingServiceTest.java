package np.org.nrb.pars.ai.ingestion;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkingServiceTest {

    private final ParsAiProperties properties = new ParsAiProperties();
    private final ChunkingService service = new ChunkingService(properties);

    @Test
    void emptyDocumentProducesNoChunks() {
        assertTrue(service.chunk("").isEmpty());
    }

    @Test
    void preservesPageNumbersFromPdfMarkers() {
        String text = "[[PAGE 1]]\nFirst page paragraph with policy details.\n\n" +
                "[[PAGE 2]]\nSecond page paragraph about exchange rates.";
        List<ChunkingService.Chunk> chunks = service.chunk(text);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().anyMatch(c -> c.page() != null && c.page() == 1));
        assertTrue(chunks.stream().anyMatch(c -> c.page() != null && c.page() == 2));
    }

    @Test
    void hardSplitsOversizedParagraphs() {
        properties.setChunkSizeTokens(20);
        properties.setChunkOverlapTokens(5);
        String huge = "A".repeat(500);
        var chunks = service.chunk(huge);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().allMatch(c -> c.text().length() <= 80));
    }

    @Test
    void respectsConfiguredChunkSizeAndOverlap() {
        properties.setChunkSizeTokens(20);
        properties.setChunkOverlapTokens(5);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            text.append("Paragraph ").append(i).append(" describes a distinct numbered policy condition for testing.\n\n");
        }
        List<ChunkingService.Chunk> chunks = service.chunk(text.toString());
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().allMatch(c -> c.text().length() <= 100));
    }
}
