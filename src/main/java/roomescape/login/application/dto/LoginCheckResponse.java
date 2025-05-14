package roomescape.login.application.dto;

import roomescape.member.domain.Member;

public record LoginCheckResponse(
        String name
) {
    public static LoginCheckResponse from(final Member member) {
        return new LoginCheckResponse(member.getNameValue());
    }
}
