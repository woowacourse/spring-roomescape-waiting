package roomescape.reservation.domain.repository;

import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long timeId);

    ReservationTime getById(Long timeId);

    List<ReservationTime> findAllByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();
}
