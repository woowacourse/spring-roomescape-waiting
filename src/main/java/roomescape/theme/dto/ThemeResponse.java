package roomescape.theme.dto;

import java.util.List;
import roomescape.theme.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String imgUrl
) {
    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getImgUrl()
        );
    }

    public static List<ThemeResponse> fromAll(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
