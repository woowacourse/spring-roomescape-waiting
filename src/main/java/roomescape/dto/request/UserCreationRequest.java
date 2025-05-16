package roomescape.dto.request;

import roomescape.domain.Role;

public record UserCreationRequest(
        Role role,
        String name,
        String email,
        String password
) {

}
