package roomescape.controller.response;

import roomescape.service.result.MemberResult;

public record LoginMemberResponse(Long id, String name, String email) {

    public static LoginMemberResponse from(MemberResult memberResult) {
        return new LoginMemberResponse(memberResult.id(), memberResult.name(), memberResult.email());
    }
}
