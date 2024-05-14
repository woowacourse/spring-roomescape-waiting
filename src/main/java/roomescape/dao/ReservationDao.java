package roomescape.dao;

import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.AvailableReservationTimeSearch;
import roomescape.dto.reservation.ReservationExistenceCheck;
import roomescape.dto.reservation.ReservationFilterParam;

import java.util.List;

public interface ReservationDao {

    Reservation save(final Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllBy(final ReservationExistenceCheck reservationExistenceCheck);

    List<Reservation> findAllBy(final ReservationFilterParam reservationFilterParam);

    boolean existById(final Long id);

    void deleteById(final Long id);

    int countByTimeId(final Long timeId);

    List<Long> findTimeIds(final AvailableReservationTimeSearch availableReservationTimeSearch);
}
