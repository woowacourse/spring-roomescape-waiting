package roomescape.domain.reservation.dto;

import roomescape.domain.member.Member;
import roomescape.domain.reservation.slot.ReservationSlot;

public record WaitingReadOnly(
        Long id,
        Member member,
        ReservationSlot slot
) {
}
