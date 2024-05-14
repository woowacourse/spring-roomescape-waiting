package roomescape.reservation.dto.response;

import roomescape.member.domain.Member;

public record FindMembersResponse(Long id,
                                  String name) {
    public static FindMembersResponse of(final Member member) {
        return new FindMembersResponse(member.getId(), member.getName());
    }
}
