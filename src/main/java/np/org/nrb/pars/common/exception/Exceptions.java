package np.org.nrb.pars.common.exception;

import org.springframework.http.HttpStatus;

public final class Exceptions {
    private Exceptions() {
    }

    public static ApiException notFound(String message) {
        return new ApiException("NOT_FOUND", message, HttpStatus.NOT_FOUND.value());
    }

    public static ApiException conflict(String message) {
        return new ApiException("CONFLICT", message, HttpStatus.CONFLICT.value());
    }

    public static ApiException optimisticLock() {
        return new ApiException("OPTIMISTIC_LOCK", "The record was updated by another user. Reload and try again.",
                HttpStatus.CONFLICT.value());
    }

    public static ApiException locked(String message) {
        return new ApiException("LOCKED", message, HttpStatus.LOCKED.value());
    }

    public static ApiException unauthorized(String message) {
        return new ApiException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED.value());
    }

    public static ApiException forbidden(String message) {
        return new ApiException("FORBIDDEN", message, HttpStatus.FORBIDDEN.value());
    }

    public static ApiException badRequest(String message) {
        return new ApiException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST.value());
    }

    public static ApiException tooManyRequests(String message) {
        return new ApiException("RATE_LIMITED", message, HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public static ApiException userExpired(String message) {
        return new ApiException("USER_EXPIRED", message, HttpStatus.FORBIDDEN.value());
    }
}
