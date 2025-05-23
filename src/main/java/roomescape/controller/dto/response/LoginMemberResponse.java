package roomescape.controller.dto.response;

import roomescape.service.dto.result.MemberResult;

public record LoginMemberResponse(Long id, String name, String email) {

    public static LoginMemberResponse from(MemberResult memberResult) {
        return new LoginMemberResponse(memberResult.id(), memberResult.name(), memberResult.email());
    }
}
