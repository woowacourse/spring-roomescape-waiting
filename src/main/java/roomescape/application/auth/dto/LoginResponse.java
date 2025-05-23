package roomescape.application.auth.dto;

import roomescape.application.dto.MemberServiceResponse;

public record LoginResponse(String name) {

    public static LoginResponse from(MemberServiceResponse memberServiceResponse) {
        return new LoginResponse(memberServiceResponse.name());
    }
}
