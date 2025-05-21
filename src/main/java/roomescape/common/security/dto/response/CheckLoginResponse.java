package roomescape.common.security.dto.response;

import roomescape.member.domain.Member;

public record CheckLoginResponse(String name) {

    public static CheckLoginResponse from(final Member member) {
        return new CheckLoginResponse(member.getName());
    }
}
