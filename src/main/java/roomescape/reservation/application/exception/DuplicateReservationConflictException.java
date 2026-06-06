package roomescape.reservation.application.exception;

public class DuplicateReservationConflictException extends RuntimeException {
  public DuplicateReservationConflictException(String message) {
    super(message);
  }
}
