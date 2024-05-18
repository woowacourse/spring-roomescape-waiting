package roomescape.presentation.dto;

import roomescape.domain.member.Role;

public record Accessor(Long id, String email, String name, Role role) {
}
