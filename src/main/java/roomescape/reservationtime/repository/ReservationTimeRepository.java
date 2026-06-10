package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservedTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    List<ReservedTime> findReservedTimes(LocalDate date, Long themeId);

    void delete(Long id);

    boolean existByStartAt(LocalTime startAt);
}
