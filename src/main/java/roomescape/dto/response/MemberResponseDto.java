package roomescape.dto.response;

import roomescape.model.Member;
import roomescape.model.Role;

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
