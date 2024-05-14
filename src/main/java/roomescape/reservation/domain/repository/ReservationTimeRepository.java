package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long timeId);

    void deleteById(long timeId);

    boolean existsByStartAt(LocalTime time);

    Set<ReservationTime> findReservedTime(LocalDate date, long themeId);
}
