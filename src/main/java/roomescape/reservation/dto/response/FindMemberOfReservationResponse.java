package roomescape.reservation.dto.response;

import roomescape.member.domain.Member;

public record FindMemberOfReservationResponse(Long id,
                                              String name) {
    public static FindMemberOfReservationResponse from(final Member member) {
        return new FindMemberOfReservationResponse(member.getId(), member.getName());
    }
}
