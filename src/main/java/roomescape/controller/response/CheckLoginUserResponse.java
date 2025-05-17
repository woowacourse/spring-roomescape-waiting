package roomescape.controller.response;

import roomescape.service.result.MemberResult;

public record CheckLoginUserResponse(String name) {

    public static CheckLoginUserResponse from(MemberResult memberResult) {
        return new CheckLoginUserResponse(memberResult.name());
    }
}
