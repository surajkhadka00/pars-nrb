package np.org.nrb.pars.common.exception;

public class ApiException extends RuntimeException {
    private final String code;
    private final int httpStatus;
    private final Object data;

    public ApiException(String code, String message, int httpStatus) {
        this(code, message, httpStatus, null);
    }

    public ApiException(String code, String message, int httpStatus, Object data) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Object getData() {
        return data;
    }
}
