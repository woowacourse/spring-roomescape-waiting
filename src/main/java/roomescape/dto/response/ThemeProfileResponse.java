package roomescape.dto.response;

import roomescape.domain.Theme;

public record ThemeProfileResponse(
        Long id,
        String name
) {

    public ThemeProfileResponse(Theme theme) {
        this(theme.getId(), theme.getName());
    }
}
