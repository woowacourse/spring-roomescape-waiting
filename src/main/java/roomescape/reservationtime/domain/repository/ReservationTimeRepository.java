package roomescape.reservationtime.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository {
    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    List<AvailableReservationTime> findAvailableByThemeAndDate(Long themeId, LocalDate date);

    ReservationTime save(ReservationTime time);

    Integer delete(Long id);

    Boolean existsByStartAt(LocalTime startAt);
}
