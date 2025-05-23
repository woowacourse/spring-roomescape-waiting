package roomescape.auth.controller.dto.response;

import roomescape.member.entity.Member;

public record LoginResponse(String name) {

    public static LoginResponse from(final Member member) {
        return new LoginResponse(member.getName());
    }
}
