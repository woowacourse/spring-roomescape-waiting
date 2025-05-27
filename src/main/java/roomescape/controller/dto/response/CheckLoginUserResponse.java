package roomescape.controller.dto.response;

import roomescape.service.dto.result.MemberResult;

public record CheckLoginUserResponse(String name) {

    public static CheckLoginUserResponse from(MemberResult memberResult) {
        return new CheckLoginUserResponse(memberResult.name());
    }
}
