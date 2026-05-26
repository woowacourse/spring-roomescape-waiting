package roomescape.dto.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String name
) {
}
