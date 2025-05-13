package roomescape.reservationtime;

import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeRepository {

    Long save(ReservationTime reservationTime);

    ReservationTime findById(Long id);
    List<ReservationTime> findAll();

    void deleteById(Long id);

    Boolean existsById(Long id);
    Boolean existsByStartAt(LocalTime startAt);
}
