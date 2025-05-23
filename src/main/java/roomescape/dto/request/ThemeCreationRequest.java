package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ThemeCreationRequest(
        @NotBlank(message = "이름은 빈 값이나 공백값을 허용하지 않습니다.")
        @Length(max = 50, message = "50자를 초과한 값은 허용하지 않습니다.")
        String name,

        @NotBlank(message = "설명은 빈 값이나 공백값을 허용하지 않습니다.")
        String description,

        @NotBlank(message = "섬네일은 빈 값이나 공백값을 허용하지 않습니다.")
        @Length(max = 600, message = "600자를 초과한 값은 허용하지 않습니다.")
        String thumbnail
) {

}
