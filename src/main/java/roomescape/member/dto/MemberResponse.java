package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberResponse(
        Long id,
        String name,
        String email
) {

    public static MemberResponse fromEntity(final Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }
}
