package roomescape.dto.response;

import roomescape.domain.Member;
import roomescape.domain.Role;

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
