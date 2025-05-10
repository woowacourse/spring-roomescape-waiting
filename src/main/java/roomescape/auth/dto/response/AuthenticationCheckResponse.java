package roomescape.auth.dto.response;

import roomescape.member.model.Member;

public record AuthenticationCheckResponse(String name) {

    public static AuthenticationCheckResponse from(Member member) {
        return new AuthenticationCheckResponse(member.getName());
    }
}
