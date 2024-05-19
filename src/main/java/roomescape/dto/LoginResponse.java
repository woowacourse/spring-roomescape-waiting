package roomescape.dto;

import roomescape.domain.LoginMember;

public record LoginResponse(String name) {
    public static LoginResponse from(LoginMember loginMember) {
        return new LoginResponse(loginMember.getName().getValue());
    }
}
