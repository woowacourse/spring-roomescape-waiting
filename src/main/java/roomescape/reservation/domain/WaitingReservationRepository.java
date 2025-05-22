package roomescape.reservation.domain;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    Integer findMaxWaitingOrderByReservationId(Long id);
}
