package roomescape.reservation.service.dto;

import roomescape.member.domain.Member;

public record ReservationChangeCommand(
    Long id,
    Member requester,
    Long dateId,
    Long timeId
) {

}
