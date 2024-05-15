package roomescape.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.RoomTheme;

public record RoomThemeCreateRequest(
        @NotBlank
        String name,
        @NotBlank
        String description,
        @NotBlank
        String thumbnail) {
    public RoomTheme toRoomTheme() {
        return new RoomTheme(name, description, thumbnail);
    }
}
