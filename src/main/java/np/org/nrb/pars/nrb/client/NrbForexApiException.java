package np.org.nrb.pars.nrb.client;

/** Error returned by the official NRB Forex API or by the client's validation layer. */
public class NrbForexApiException extends RuntimeException {

    private final int statusCode;
    private final String validationDetails;

    public NrbForexApiException(String message) {
        this(message, 0, null);
    }

    public NrbForexApiException(String message, int statusCode, String validationDetails) {
        super(message);
        this.statusCode = statusCode;
        this.validationDetails = validationDetails;
    }

    public int statusCode() {
        return statusCode;
    }

    public String validationDetails() {
        return validationDetails;
    }
}
