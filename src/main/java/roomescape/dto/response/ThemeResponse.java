package roomescape.dto.response;

import roomescape.domain.Theme;

import java.util.List;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String imgUrl
) {
    public static List<ThemeResponse> fromAll(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getImgUrl()
        );
    }
}
