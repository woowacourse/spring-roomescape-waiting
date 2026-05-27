package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.dto.ReservationTimeAvailability;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    List<ReservationTimeAvailability> findAllByDateAndThemeIdWithAvailability(LocalDate date, Long themeId);

    boolean existsByStartAt(LocalTime startAt);

    boolean cancelById(Long id, LocalDateTime now);
}
