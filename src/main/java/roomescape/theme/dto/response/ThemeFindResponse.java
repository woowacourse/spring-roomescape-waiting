package roomescape.theme.dto.response;

import roomescape.theme.Theme;

import java.util.List;

public record ThemeFindResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static List<ThemeFindResponse> from(List<Theme> themes) {
        return themes.stream()
                .map(theme -> new ThemeFindResponse(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnailUrl())
                )
                .toList();
    }
}
