package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.reservation.Description;
import roomescape.domain.reservation.ThemeName;
import roomescape.domain.reservation.Thumbnail;

public record ThemeRequest(
        @NotBlank(message = "이름을 입력해주세요") ThemeName name,
        @NotNull Description description,
        @NotNull Thumbnail thumbnail
) {
}
