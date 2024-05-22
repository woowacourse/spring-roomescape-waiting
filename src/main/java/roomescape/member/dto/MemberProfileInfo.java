package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberProfileInfo(
        Long id,
        String name,
        String email
) {
    public static MemberProfileInfo from(Member member) {
        return new MemberProfileInfo(member.getId(), member.getName(), member.getEmail());
    }
}
