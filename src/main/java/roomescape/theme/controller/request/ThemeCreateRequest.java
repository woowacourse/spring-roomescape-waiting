package roomescape.theme.controller.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ThemeCreateRequest(
        @NotBlank
        String name,
        @NotBlank
        String description,
        @NotBlank
        @URL
        String thumbnail
) {
}
