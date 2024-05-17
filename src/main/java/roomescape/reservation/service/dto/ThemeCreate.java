package roomescape.reservation.service.dto;

import roomescape.reservation.controller.dto.ThemeRequest;

public record ThemeCreate(String name, String description, String thumbnail) {
    public static ThemeCreate from(ThemeRequest themeRequest) {
        return new ThemeCreate(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
    }
}

