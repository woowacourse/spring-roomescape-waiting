package roomescape.presentation.request;

import jakarta.validation.constraints.NotEmpty;

public record CreateThemeRequest(
    @NotEmpty
    String name,

    @NotEmpty
    String description,

    @NotEmpty
    String thumbnail
) {

}
