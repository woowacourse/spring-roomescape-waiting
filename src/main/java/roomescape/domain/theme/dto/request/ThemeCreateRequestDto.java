package roomescape.domain.theme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ThemeCreateRequestDto(
    @NotBlank(message = "테마 이름은 필수입니다.")
    @Size(max = 255, message = "테마 이름의 길이는 255 이하여야 합니다.")
    String name,

    @NotBlank(message = "테마 설명은 필수입니다.")
    @Size(max = 255, message = "테마 설명의 길이는 255 이하여야 합니다.")
    String description,

    @NotBlank(message = "테마 이미지 URL은 필수입니다.")
    @Size(max = 2000, message = "테마 이미지 URL의 길이는 2000 이하여야 합니다.")
    @URL(message = "테마 이미지 URL 형식이 올바르지 않습니다.")
    String imageUrl
) {

}
