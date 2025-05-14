package roomescape.dao;

import java.util.List;
import java.util.Optional;
import roomescape.dto.request.ReservationSearchFilter;
import roomescape.model.Reservation;

public interface ReservationDao {
    List<Reservation> findAll(ReservationSearchFilter reservationSearchFilter);

    Long saveReservation(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findByDateAndTime(Reservation reservation);
}
