package roomescape.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @NotNull
        @NotEmpty
        String password,
        @NotNull
        @NotEmpty
        String email) {
}
