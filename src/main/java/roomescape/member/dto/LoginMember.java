package roomescape.member.dto;

import roomescape.auth.Role;

public record LoginMember(Long id, Role role, String name, String email) {
}
