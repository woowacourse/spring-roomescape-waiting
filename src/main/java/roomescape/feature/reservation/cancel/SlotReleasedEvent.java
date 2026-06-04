package roomescape.feature.reservation.cancel;

import roomescape.feature.reservation.domain.Slot;

public record SlotReleasedEvent(
        Slot slot
) {
}
