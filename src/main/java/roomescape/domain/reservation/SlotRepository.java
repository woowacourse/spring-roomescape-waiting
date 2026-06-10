package roomescape.domain.reservation;

import roomescape.domain.RoomEscapeException;
import roomescape.domain.theme.Theme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface SlotRepository {
    List<Slot> findAll();

    List<Slot> findAllByName(String name);

    Optional<Slot> findById(long slotId);

    Optional<Slot> findByDateAndTimeAndTheme(ReservationDate date, ReservationTime time, Theme theme);

    Slot save(Slot slot);

    Slot update(long id, Slot target);

    void deleteById(long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    default Slot getById(long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약 슬롯을 찾을 수 없습니다. : " + id));
    }

    default Slot findOrCreate(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(Slot.create(date, time, theme, now)));
    }
}
