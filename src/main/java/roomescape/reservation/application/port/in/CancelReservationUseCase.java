package roomescape.reservation.application.port.in;

public interface CancelReservationUseCase {
    void deleteById(long reservationId);
    void deleteByIdForUser(long reservationId, long memberId);
}
