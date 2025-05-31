package roomescape.dto.member;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record MemberResponseDto(
        Long id,
        String name,
        String email,
        Role role
) {

    public MemberResponseDto(Member member) {
        this(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
