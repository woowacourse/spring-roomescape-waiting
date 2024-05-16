package roomescape.reservation.dto;

import roomescape.reservation.model.Theme;

public record SaveThemeRequest(String name, String description, String thumbnail) {
    public Theme toTheme() {
        return Theme.of(name, description, thumbnail);
    }
}
