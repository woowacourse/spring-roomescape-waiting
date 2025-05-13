package roomescape.reservation.service.dto;

import roomescape.reservation.domain.Theme;

public record ThemeInfo(long id, String name, String description, String thumbnail) {

    public ThemeInfo(final Theme theme) {
        this(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
