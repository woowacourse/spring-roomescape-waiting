package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.reservation.domain.Theme;

public record ThemeRequest(
        @NotBlank(message = "name 값이 없습니다.") String name,
        @NotBlank(message = "description 값이 없습니다.") String description,
        @NotBlank(message = "thumbnail 값이 없습니다.") String thumbnail
) {
    public Theme toTheme() {
        return Theme.builder()
                .name(name)
                .description(description)
                .thumbnail(thumbnail).build();
    }
}
