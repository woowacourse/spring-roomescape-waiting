package roomescape.dto;

import roomescape.domain.Theme;

public record ThemeResponse(long id, String name, String description, String thumbnailUrl) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }
}
