package roomescape.reservation.ui.theme.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.reservation.application.theme.dto.ThemeCreateCommand;

public record ThemeCreateRequest(
        @NotNull(message = "테마명을 입력해주세요.") String name,
        @NotNull(message = "테마소개를 입력해주세요.") String description,
        @NotNull(message = "썸네일을 입력해주세요.") String thumbnail
) {

    public ThemeCreateCommand convertToCreateCommand() {
        return new ThemeCreateCommand(name, description, thumbnail);
    }
}
