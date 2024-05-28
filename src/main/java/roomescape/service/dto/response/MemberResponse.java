package roomescape.service.dto.response;

import roomescape.domain.member.Member;

public record MemberResponse(
        Long id,
        String name,
        String role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getRole());
    }
}
