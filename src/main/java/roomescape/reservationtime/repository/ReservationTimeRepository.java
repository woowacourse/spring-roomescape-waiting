package roomescape.reservationtime.repository;

import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.dto.ReservationTimeAvailability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();
    Optional<ReservationTime> findById(Long id);
    List<ReservationTimeAvailability> findAllByDateAndThemeIdWithAvailability(LocalDate date, Long themeId);

    boolean existsByStartAt(LocalTime startAt);

    boolean cancelById(Long id);
}
