package roomescape.reservation.domain;

import roomescape.reservation.domain.ReservationTime;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);
}
