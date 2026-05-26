package roomescape.theme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ThemeRequest(
        @NotBlank(message = "테마 이름은 필수값 입니다.")
        @Size(max = 30, message = "테마 이름은 30자 이하여야 합니다.")
        String name,

        @NotBlank(message = "테마 설명은 필수값 입니다.")
        @Size(max = 100, message = "테마 설명은 100자 이하여야 합니다.")
        String description,

        @NotBlank(message = "썸네일은 필수값 입니다.")
        @Size(max = 100, message = "테마 썸네일은 100자 이하여야 합니다.")
        @URL(message = "썸네일은 URL 형식이어야 합니다.")
        String thumbnail
) {
}
