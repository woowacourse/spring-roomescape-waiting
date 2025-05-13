package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberResponse(
        long id,
        String email,
        String name
) {

    public MemberResponse(final Member member) {
        this(member.getId(), member.getEmail(), member.getName());
    }
}
