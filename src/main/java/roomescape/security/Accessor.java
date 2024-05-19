package roomescape.security;

import roomescape.domain.member.Role;

public record Accessor(Long id, String name, Role role) {
}
