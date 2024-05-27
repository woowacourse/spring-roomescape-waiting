package roomescape.theme.dto.response;

import roomescape.theme.model.Theme;

public record FindThemeResponse(Long id,
                                String name,
                                String description,
                                String thumbnail) {
    public static FindThemeResponse from(final Theme theme) {
        return new FindThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());
    }
}
