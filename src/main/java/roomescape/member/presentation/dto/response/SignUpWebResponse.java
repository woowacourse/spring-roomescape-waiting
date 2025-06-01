package roomescape.member.presentation.dto.response;

import roomescape.member.domain.Member;

public record SignUpWebResponse(Long id) {

    public static SignUpWebResponse from(final Member member) {
        return new SignUpWebResponse(member.getId());
    }
}
