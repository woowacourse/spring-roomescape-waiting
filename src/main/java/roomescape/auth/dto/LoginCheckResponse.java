package roomescape.auth.dto;

import roomescape.member.domain.Member;

public record LoginCheckResponse(String name) {

    public static LoginCheckResponse from(Member member) {
        return new LoginCheckResponse(member.getName());
    }
}
