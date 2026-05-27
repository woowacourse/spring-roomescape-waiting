package roomescape.admin.theme.dto;

import java.util.List;
import roomescape.domain.theme.Theme;

public record AdminThemesResponse(
        List<AdminThemeResponse> themes
) {

    public static AdminThemesResponse from(List<Theme> themes) {
        return new AdminThemesResponse(themes.stream()
                .map(AdminThemeResponse::from)
                .toList()
        );
    }
}
