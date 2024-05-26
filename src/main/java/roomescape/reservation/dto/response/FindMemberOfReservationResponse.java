package roomescape.reservation.dto.response;

import roomescape.member.domain.Member;

public record FindMemberOfReservationResponse(Long id, String name) {
    public static FindMemberOfReservationResponse from(Member member) {
        return new FindMemberOfReservationResponse(member.getId(), member.getName());
    }
}
