package roomescape.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull
        @NotEmpty
        String name,
        @NotNull
        @NotEmpty
        String email,
        @NotNull
        @NotEmpty
        String password) {
}
