package roomescape.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.service.command.ThemeCommand;

public record ThemeRequest(
        @NotBlank(message = "테마 이름은 필수입니다.")
        String name,
        String thumbnailUrl,
        String description
) {
        public static ThemeCommand toCommand(ThemeRequest request) {
                return new ThemeCommand(
                        request.name(),
                        request.thumbnailUrl(),
                        request.description()
                );
        }
}
