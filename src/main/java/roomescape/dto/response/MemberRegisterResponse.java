package roomescape.dto.response;

import roomescape.domain.Member;

public record MemberRegisterResponse(
        long id,
        String email,
        String name
) {
    public static MemberRegisterResponse from(final Member member) {
        return new MemberRegisterResponse(member.getId(), member.getEmail(), member.getName());
    }
}
