package roomescape.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ThemeRequest(
        @NotNull
        @NotEmpty
        String name,
        @NotNull
        @NotEmpty
        String description,
        @NotNull
        @NotEmpty
        String thumbnail) {
}
