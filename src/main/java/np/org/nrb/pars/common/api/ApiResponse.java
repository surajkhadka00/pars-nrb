package np.org.nrb.pars.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        ApiMeta meta,
        OffsetDateTime timestamp,
        String traceId
) {
    private static final ZoneId KATHMANDU = ZoneId.of("Asia/Kathmandu");

    public static <T> ApiResponse<T> ok(T data) {
        return ok("OK", "Success", data, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ok("OK", message, data, null);
    }

    public static <T> ApiResponse<List<T>> paged(List<T> data, ApiMeta meta) {
        return ok("OK", "Success", data, meta);
    }

    public static <T> ApiResponse<T> ok(String code, String message, T data, ApiMeta meta) {
        return new ApiResponse<>(true, code, message, data, meta, now(), currentTraceId());
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data, null, now(), currentTraceId());
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(KATHMANDU);
    }

    private static String currentTraceId() {
        String id = MDC.get("traceId");
        return id == null || id.isBlank() ? null : id;
    }
}
