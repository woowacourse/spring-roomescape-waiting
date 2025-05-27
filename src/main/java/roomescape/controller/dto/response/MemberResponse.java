package roomescape.controller.dto.response;

import roomescape.service.dto.result.MemberResult;

public record MemberResponse(Long id,
                             String name,
                             String email) {

    public static MemberResponse from(MemberResult memberResult) {
        return new MemberResponse(memberResult.id(), memberResult.name(), memberResult.email());
    }
}
