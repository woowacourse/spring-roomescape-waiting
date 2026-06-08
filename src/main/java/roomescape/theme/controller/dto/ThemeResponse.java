package roomescape.theme.controller.dto;

import roomescape.reservation.service.dto.PopularThemeResult;
import roomescape.theme.domain.Theme;

public record ThemeResponse(Long id, String name, String description, String thumbnailUrl) {

    public static ThemeResponse from(PopularThemeResult theme) {
        return new ThemeResponse(theme.id(), theme.name(), theme.description(), theme.thumbnailUrl());
    }

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }
}
