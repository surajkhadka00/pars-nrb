package np.org.nrb.pars.ai.rag;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HybridRetrievalServiceTest {

    private final HybridRetrievalService service = new HybridRetrievalService(null, null, null, null, null);

    private HybridRetrievalService.Evidence evidence(String text, double score) {
        return new HybridRetrievalService.Evidence(text, UUID.randomUUID(), "Policy", 1, 1, "Section",
                UUID.nameUUIDFromBytes(text.getBytes()), score, "PUBLISHED", score,
                "https://www.nrb.org.np/api/forex/v1/rates");
    }

    @Test
    void rrfFusesTwoRankingsAndDeduplicates() {
        var vector = List.of(evidence("A", 0.9), evidence("B", 0.8));
        var keyword = List.of(evidence("B", 0.7), evidence("C", 0.6));
        List<HybridRetrievalService.Evidence> fused = service.fuse(vector, keyword, 5);
        assertEquals(3, fused.size());
        assertTrue(fused.get(0).score() > fused.get(1).score());
    }

    @Test
    void rrfRespectsFinalPassageLimit() {
        var a = List.of(evidence("A", 0.9), evidence("B", 0.8), evidence("C", 0.7));
        List<HybridRetrievalService.Evidence> fused = service.fuse(a, List.of(), 2);
        assertEquals(2, fused.size());
    }

    @Test
    void emptyRankingsRemainEmpty() {
        assertTrue(service.fuse(List.of(), List.of(), 5).isEmpty());
    }
}
