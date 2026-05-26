package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import roomescape.domain.Theme;

public record ThemeRequest(
        @NotBlank(message = "테마명을 입력해 주세요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9 ]{2,30}$",
                message = "테마명은 2자 이상 30자 이하의 한글, 영문, 숫자 또는 공백만 사용할 수 있습니다."
        )
        String name,

        @NotBlank(message = "테마에 대한 설명을 입력해 주세요.")
        @Size(min = 10, max = 500, message = "테마 설명은 10자 이상 500자 이하로 입력해 주세요.")
        String description,

        @NotBlank(message = "썸네일 링크를 입력해 주세요.")
        @Pattern(
                regexp = "^https://.+",
                message = "썸네일 링크는 https로 시작해야 합니다."
        )
        String thumbnail
) {
    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
