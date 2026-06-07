package roomescape.reservation.repository;

import roomescape.reservation.domain.ReservationHistory;

import java.util.List;

public interface ReservationHistoryRepository {

    List<ReservationHistory> findByName(String name);

    boolean saveFromReservation(Long reservationId);
}
