package roomescape.reservation.repository;

import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long reservationTimeId);

    ReservationTime save(ReservationTime reservationTime);

    int deleteById(Long reservationTimeId);

    boolean existByStartAt(LocalTime startAt);
}
