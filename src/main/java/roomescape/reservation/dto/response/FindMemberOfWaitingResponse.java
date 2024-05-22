package roomescape.reservation.dto.response;

import roomescape.member.domain.Member;

public record FindMemberOfWaitingResponse(Long id,
                                          String name) {
    public static FindMemberOfWaitingResponse from(final Member member) {
        return new FindMemberOfWaitingResponse(member.getId(), member.getName());
    }
}
