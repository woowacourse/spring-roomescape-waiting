package roomescape.auth.dto.response;

import roomescape.member.domain.Member;

public record MemberCreationUpResponse(
    String email,
    boolean isSuccess
) {

    public static MemberCreationUpResponse of(Member member, boolean isSuccess) {
        return new MemberCreationUpResponse(member.getEmail(), isSuccess);
    }
}
