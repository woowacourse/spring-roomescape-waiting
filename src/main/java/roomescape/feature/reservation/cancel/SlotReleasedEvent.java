package roomescape.feature.reservation.cancel;

import roomescape.feature.reservation.domain.SlotKey;

public record SlotReleasedEvent(
        SlotKey slotKey
) {
}
