package roomescape.login.presentation.response;

import roomescape.member.business.domain.Member;

public record LoginCheckResponse(
        String name
) {
    public static LoginCheckResponse from(final Member member) {
        return new LoginCheckResponse(member.getName());
    }
}
