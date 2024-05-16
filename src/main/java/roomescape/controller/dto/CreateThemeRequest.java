package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

public record CreateThemeRequest(
    @NotBlank(message = "null이거나 비어있을 수 없습니다.")
    @Length(min = 1, max = 50)
    String name,

    @NotBlank(message = "null이거나 비어있을 수 없습니다.")
    @Length(min = 1, max = 100)
    String description,

    @NotBlank(message = "null이거나 비어있을 수 없습니다.")
    @URL(message = "올바른 URL 형식이 아닙니다.")
    String thumbnail
) {

}
