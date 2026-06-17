package roomescape.theme.application.dto.response;

import java.util.List;
import roomescape.theme.domain.Theme;

public record ThemeFindResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        int price
) {
    public ThemeFindResponse(Long id, String name, String description, String thumbnailUrl) {
        this(id, name, description, thumbnailUrl, 0);
    }

    public static List<ThemeFindResponse> from(List<Theme> themes) {
        return themes.stream()
                .map(theme -> new ThemeFindResponse(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnailUrl(),
                        theme.getPrice())
                )
                .toList();
    }
}
