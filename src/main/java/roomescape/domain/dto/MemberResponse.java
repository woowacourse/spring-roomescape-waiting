package roomescape.domain.dto;

import roomescape.domain.Member;

public record MemberResponse(Long id, String email, String name) {
    public static MemberResponse from(final Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName()
        );
    }
}
