package roomescape.reservationslot.service.support;

import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private long nextId = 1L;
    private final List<ReservationSlot> slots = new ArrayList<>();

    @Override
    public ReservationSlot findOrCreate(final LocalDate date, final ReservationTime time, final Theme theme) {
        return findByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())
                .orElseGet(() -> {
                    ReservationSlot slot = ReservationSlot.of(nextId++, date, time, theme);
                    slots.add(slot);
                    return slot;
                });
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(
            final LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        return slots.stream()
                .filter(slot -> slot.getDate().equals(date))
                .filter(slot -> slot.getTime().getId().equals(timeId))
                .filter(slot -> slot.getTheme().getId().equals(themeId))
                .findFirst();
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeIdForUpdate(
            final LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        return findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<ReservationSlot> findByIdForUpdate(final Long slotId) {
        return slots.stream()
                .filter(slot -> slot.getId().equals(slotId))
                .findFirst();
    }

    public void add(final ReservationSlot slot) {
        slots.add(slot);
        nextId = Math.max(nextId, slot.getId() + 1);
    }
}
