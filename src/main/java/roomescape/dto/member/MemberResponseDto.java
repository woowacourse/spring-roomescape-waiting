package roomescape.dto.member;

import roomescape.domain.member.Role;

public record MemberResponseDto(long id, String name, String email, Role role) {
}
