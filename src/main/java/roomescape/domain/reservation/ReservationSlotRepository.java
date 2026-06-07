package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findById(Long id);

    Optional<ReservationSlot> findByIdForUpdate(Long id);

    List<ReservationCountResult> findWaitingCountsByThemeIdAndDate(Long themeId, LocalDate date);

    ReservationSlot save(ReservationSlot reservation);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);
}
