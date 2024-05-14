package roomescape.service.dto;

import roomescape.domain.Member;

public record MemberResponse(
        Long id,
        String name,
        String role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getRole().name());
    }
}
