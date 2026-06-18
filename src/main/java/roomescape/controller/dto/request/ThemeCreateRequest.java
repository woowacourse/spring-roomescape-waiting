package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ThemeCreateRequest {
    @NotNull(message = "이름은 필수로 입력해야 합니다")
    private final String name;

    @NotNull(message = "설명은 필수로 입력해야 합니다")
    private final String description;

    @NotNull(message = "URL은 필수로 입력해야 합니다")
    private final String thumbnailUrl;
}
