package roomescape.dto.response;

import roomescape.domain.Member;

public record AuthenticationCheckResponse(String name) {

    public static AuthenticationCheckResponse from(Member member) {
        return new AuthenticationCheckResponse(member.getName());
    }
}
