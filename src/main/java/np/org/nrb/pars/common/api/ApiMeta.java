package np.org.nrb.pars.common.api;

import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;

public record ApiMeta(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        OffsetDateTime generatedAt
) {
    public static ApiMeta from(Page<?> page) {
        return new ApiMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), null);
    }

    public static ApiMeta report(OffsetDateTime generatedAt) {
        return new ApiMeta(null, null, null, null, generatedAt);
    }
}
