package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ThemeCreateRequest(
        @NotNull(message = "THEME_NAME_NULL_OR_BLANK")
        @NotBlank(message = "THEME_NAME_NULL_OR_BLANK")
        String name,

        @NotNull(message = "DESCRIPTION_NULL_OR_BLANK")
        @NotBlank(message = "DESCRIPTION_NULL_OR_BLANK")
        String description,

        @NotNull(message = "THUMBNAIL_URL_NULL_OR_BLANK")
        @NotBlank(message = "THUMBNAIL_URL_NULL_OR_BLANK")
        String thumbnailUrl
) {
}
