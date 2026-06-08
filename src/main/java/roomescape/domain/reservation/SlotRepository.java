package roomescape.domain.reservation;

import roomescape.common.exception.NotFoundException;
import roomescape.domain.theme.Theme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                .orElseThrow(() -> new NotFoundException("존재하지 않는 슬롯입니다."));
    }

    default Slot findOrCreate(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(Slot.create(date, time, theme, now)));
    }
}
