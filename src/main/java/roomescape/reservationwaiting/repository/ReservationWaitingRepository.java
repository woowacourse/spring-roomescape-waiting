package roomescape.reservationwaiting.repository;

import roomescape.reservationwaiting.domain.ReservationWaiting;

import java.util.List;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(Long id);

    List<Long> calculateTurn(String name);

    List<ReservationWaiting> findByName(String name);

    boolean existsByNameAndReservationId(String name, Long reservationId);

    ReservationWaiting findReservationWaitingById(Long reservationWaitingId);
}
