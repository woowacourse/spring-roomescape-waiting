package roomescape.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record SignupRequest(
        @NotEmpty
        @Email
        String email,

        @NotEmpty
        String password,

        @NotEmpty
        String name
) {
}
