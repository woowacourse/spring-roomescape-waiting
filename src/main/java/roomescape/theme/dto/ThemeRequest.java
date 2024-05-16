package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.theme.domain.Description;
import roomescape.theme.domain.ThemeName;

public record ThemeRequest(
        @NotBlank(message = "테마의 이름은 null 또는 공백일 수 없습니다.")
        @Size(min = ThemeName.MIN_LENGTH, max = ThemeName.MAX_LENGTH, message = "테마의 이름은 " + ThemeName.MIN_LENGTH + "~" + ThemeName.MAX_LENGTH + "글자 사이여야 합니다.")
        String name,
        @NotBlank(message = "테마의 설명은 null 또는 공백일 수 없습니다.")
        @Size(min = Description.MIN_LENGTH, max = Description.MAX_LENGTH, message = "테마의 설명은 " + Description.MIN_LENGTH + "~" + Description.MAX_LENGTH + "글자 사이여야 합니다.")
        String description,
        @NotBlank(message = "테마의 쌈네일은 null 또는 공백일 수 없습니다.")
        String thumbnail
) {
}
