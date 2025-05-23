package roomescape.controller.dto.response;

import roomescape.service.dto.result.MemberResult;

public record RegisterUserResponse(Long id, String name, String email) {

    public static RegisterUserResponse from(MemberResult memberResult) {
        return new RegisterUserResponse(memberResult.id(), memberResult.name(), memberResult.email());
    }
}
