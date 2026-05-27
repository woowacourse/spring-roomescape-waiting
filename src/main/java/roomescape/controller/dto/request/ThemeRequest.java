package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ThemeRequest(
        @NotNull(message = "테마 이름은 비어 있을 수 없습니다.")
        String name,

        @NotNull(message = "테마 설명은 비어 있을 수 없습니다.")
        String description,

        @NotNull(message = "썸네일 이미지는 비어 있을 수 없습니다.")
        MultipartFile file
) {
}
