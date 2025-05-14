package roomescape.dao;

import java.util.List;
import java.util.Optional;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.model.Reservation;

public interface ReservationDao {
    List<Reservation> findAll(ReservationSearchDto reservationSearchDto);

    Long saveReservation(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findByDateAndTime(Reservation reservation);
}
