package roomescape.service.event;

import roomescape.domain.slot.EventSlot;

public record ReservationChangeEvent(
        EventSlot eventSlot
) {
}
