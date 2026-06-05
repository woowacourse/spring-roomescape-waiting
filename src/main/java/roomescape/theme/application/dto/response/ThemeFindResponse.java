package roomescape.theme.application.dto.response;

import java.util.List;
import roomescape.theme.domain.Theme;

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
