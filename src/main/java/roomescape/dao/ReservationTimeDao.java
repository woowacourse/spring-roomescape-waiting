package roomescape.dao;

import roomescape.domain.reservation.ReservationTime;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeDao {

    ReservationTime save(final ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    Optional<ReservationTime> findById(final Long id);
}
