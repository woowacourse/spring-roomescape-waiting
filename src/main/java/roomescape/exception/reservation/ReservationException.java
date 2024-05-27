package roomescape.exception.reservation;

public abstract class ReservationException extends RuntimeException {
    private final String logMessage;

    protected ReservationException(String message, String logMessage) {
        super(message);
        this.logMessage = logMessage;
    }

    public String getLogMessage() {
        return logMessage;
    }
}
