package roomescape.service;

public interface ReservationApprovalService {
    void cancelReservation(long reservationId, long memberId);

    void approveWaiting(long reservationWaitingId);
}
