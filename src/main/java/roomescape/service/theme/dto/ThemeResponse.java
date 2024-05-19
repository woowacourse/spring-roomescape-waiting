package roomescape.service.theme.dto;

import roomescape.domain.theme.Theme;

public record ThemeResponse(long id, String name, String description, String thumbnail) {
    public ThemeResponse(Theme theme) {
        this(theme.getId(), theme.getName().getValue(), theme.getDescription().getValue(),
                theme.getThumbnail().getValue());
    }
}
