package roomescape.slot.domain;

import roomescape.reservation.domain.Reservations;
import roomescape.slot.exception.ReservationSlotException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

public record RescheduleSlots(
        Map<Long, ReservationSlot> slots
) {

    public RescheduleSlots {
        slots = Map.copyOf(slots);
    }

    public static RescheduleSlots of(List<ReservationSlot> slots) {
        return new RescheduleSlots(
                slots.stream().collect(toMap(ReservationSlot::getId, s -> s))
        );
    }

    public Reservations reschedule(Long currentSlotId, Long newSlotId, Long reservationId, String requesterName) {
        ReservationSlot currentSlot = findById(currentSlotId);
        ReservationSlot newSlot = findById(newSlotId);

        return currentSlot.reschedule(newSlot, reservationId, requesterName);
    }

    public Reservations rescheduleByManager(Long currentSlotId, Long newSlotId, Long reservationId) {
        ReservationSlot currentSlot = findById(currentSlotId);
        ReservationSlot newSlot = findById(newSlotId);

        return currentSlot.rescheduleByManager(newSlot, reservationId);
    }

    private ReservationSlot findById(Long id) {
        return Optional.ofNullable(slots.get(id))
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
    }

}
