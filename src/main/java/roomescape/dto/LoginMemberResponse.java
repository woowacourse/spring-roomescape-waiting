package roomescape.dto;

import roomescape.domain.LoginMember;

public record LoginMemberResponse(long id, String name) {
    public static LoginMemberResponse from(LoginMember loginMember) {
        return new LoginMemberResponse(
                loginMember.getId(),
                loginMember.getName()
        );
    }
}
