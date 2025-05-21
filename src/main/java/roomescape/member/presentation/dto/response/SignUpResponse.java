package roomescape.member.presentation.dto.response;

import roomescape.member.domain.Member;

public record SignUpResponse(Long id) {

    public static SignUpResponse from(final Member member) {
        return new SignUpResponse(member.getId());
    }
}
