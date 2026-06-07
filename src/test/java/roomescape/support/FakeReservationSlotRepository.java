package roomescape.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.repository.reservationslot.ReservationSlotRepository;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private final Map<Long, ReservationSlot> slots = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public List<ReservationSlot> findAll() {
        return new ArrayList<>(slots.values());
    }

    @Override
    public Optional<ReservationSlot> findById(final long slotId) {
        return Optional.ofNullable(slots.get(slotId));
    }

    @Override
    public Optional<ReservationSlot> findBySlot(final ReservationSlot reservationSlot) {
        return slots.values()
                .stream()
                .filter(slot -> Objects.equals(slot.getDate(), reservationSlot.getDate()))
                .filter(slot -> Objects.equals(slot.getTheme(), reservationSlot.getTheme()))
                .filter(slot -> Objects.equals(slot.getTime(), reservationSlot.getTime()))
                .findFirst();
    }

    @Override
    public ReservationSlot save(final ReservationSlot reservationSlot) {
        ReservationSlot saved = ReservationSlot.of(
                sequence++,
                reservationSlot.getDate(),
                reservationSlot.getTheme(),
                reservationSlot.getTime()
        );
        slots.put(saved.getId(), saved);
        return saved;
    }
}
