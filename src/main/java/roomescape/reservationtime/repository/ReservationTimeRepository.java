package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long timeId);

    int deleteById(long timeId);

    ReservationTime save(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);

}
