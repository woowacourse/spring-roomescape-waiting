package roomescape.dto;

import roomescape.domain.Member;

public record LoginMemberResponse(long id, String name) {
    public static LoginMemberResponse from(Member member) {
        return new LoginMemberResponse(
                member.getId(),
                member.getName().getValue()
        );
    }
}
