package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findBySchedule(Long timeId, LocalDate date, Long themeId);

    Optional<ReservationSlot> findById(Long id);

    ReservationSlot save(ReservationSlot reservation);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsBySchedule(Long timeId, Long dateId, Long themeId);
}
