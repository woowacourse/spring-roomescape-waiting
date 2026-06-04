package roomescape.domain.reservationslot;

import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository {

    ReservationSlot save(ReservationSlot reservation);

    List<ReservationSlot> findAll();

    int deleteById(Long id);

    int countByTimeId(Long timeId);

    int countByReservationDateId(Long dateId);

    int countByThemeId(Long id);

    Optional<ReservationSlot> findBySchedule(Long timeId, Long dateId, Long themeId);

    Optional<ReservationSlot> findById(Long id);

    Optional<ReservationSlot> update(Long id, ReservationSlot withoutId);

    Optional<ReservationSlot> findByScheduleToUpdate(Long timeId, Long dateId, Long themeId);
}
