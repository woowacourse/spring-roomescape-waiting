package roomescape.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.domain.theme.Theme;

@Getter
@RequiredArgsConstructor
public class ThemeResponse {
    private final long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName().getValue(), theme.getDescription(),
                theme.getThumbnailUrl().getValue());
    }
}
