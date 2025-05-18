package roomescape.presentation.dto.request;

import roomescape.domain.Role;

public record LoginMember(Long id, String name, Role role, String email) {
}
