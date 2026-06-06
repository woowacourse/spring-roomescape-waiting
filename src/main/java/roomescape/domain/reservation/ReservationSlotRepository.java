package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findBySchedule(Long timeId, LocalDate date, Long themeId);

    ReservationSlot save(ReservationSlot reservation);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);
}
