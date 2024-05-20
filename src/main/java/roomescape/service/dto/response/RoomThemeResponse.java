package roomescape.service.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.RoomTheme;

public record RoomThemeResponse(
        @NotNull
        Long id,
        @NotBlank
        String name,
        @NotBlank
        String description,
        @NotBlank
        String thumbnail) {
    public static RoomThemeResponse from(RoomTheme roomTheme) {
        return new RoomThemeResponse(
                roomTheme.getId(),
                roomTheme.getName(),
                roomTheme.getDescription(),
                roomTheme.getThumbnail());
    }
}
