package roomescape.auth.dto.response;

import roomescape.member.domain.Member;

public record MemberSignUpResponse(
    String email,
    boolean isSuccess
) {
    public static MemberSignUpResponse of(Member member, boolean isSuccess) {
        return new MemberSignUpResponse(member.getEmail(), isSuccess);
    }
}
