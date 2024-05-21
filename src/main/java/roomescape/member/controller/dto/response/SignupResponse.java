package roomescape.member.controller.dto.response;

import roomescape.member.domain.Member;

public record SignupResponse(
        String name,
        String email,
        String password
) {

    public static SignupResponse from(final Member member) {
        return new SignupResponse(member.getNameValue(), member.getEmail(), member.getPassword());
    }
}
