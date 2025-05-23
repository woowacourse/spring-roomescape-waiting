package roomescape.controller.dto.response;

import roomescape.entity.Member;

public record LoginResponse(
    String name) {

    public static LoginResponse from(Member member) {
        return new LoginResponse(member.getName());
    }
}
