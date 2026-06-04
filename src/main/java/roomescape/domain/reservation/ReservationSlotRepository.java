package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.Theme;

public interface ReservationSlotRepository {

    ReservationSlot save(ReservationSlot reservation);

    List<ReservationSlot> findAll();

    int deleteById(Long id);

    int countByTimeId(Long timeId);

    int countByReservationDateId(Long dateId);

    List<Theme> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today);

    int countByThemeId(Long id);

    boolean existsReservation(Long timeId, Long dateId, Long themeId);

    boolean existsBySchedule(Long timeId, Long dateId, Long themeId);

    Optional<ReservationSlot> findBySchedule(Long timeId, LocalDate date, Long themeId);

    Optional<ReservationSlot> findByScheduleForUpdate(Long timeId, Long dateId, Long themeId);

    Optional<ReservationSlot> findById(Long id);

    Optional<ReservationSlot> update(Long id, ReservationSlot withoutId);

    boolean existsByTimeId(Long id);

    default ReservationSlot findByScheduleOrThrow(Long timeId, LocalDate date, Long themeId) {
        return findBySchedule(timeId, date, themeId)
                .orElseThrow();
    }
}
