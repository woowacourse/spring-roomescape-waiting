package roomescape.domain.vo;

import roomescape.domain.ReservationSlot;

public record LockedReservationSlots(ReservationSlot currentSlot, ReservationSlot newSlot) {
}
