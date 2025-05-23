package roomescape.dto.member;

import roomescape.domain.member.Role;

public record MemberResponse(Long id, String name, String email, Role role) {
}
