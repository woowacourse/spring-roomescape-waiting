package roomescape.presentation.dto;

import roomescape.business.domain.Member;

public record MemberResponse(Long id, String name, String email) {

    public static MemberResponse from(final Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
