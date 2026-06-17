package roomescape.theme.presentation.dto;

import roomescape.theme.domain.Theme;

public record ThemeResponse(Long id, String name, String description, String thumbnailImgUrl, Long price) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailImgUrl(), theme.getPrice());
    }
}
