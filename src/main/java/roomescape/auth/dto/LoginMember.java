package roomescape.auth.dto;

import roomescape.auth.domain.Role;

public record LoginMember(Long id, Role role, String name, String email) {
}
