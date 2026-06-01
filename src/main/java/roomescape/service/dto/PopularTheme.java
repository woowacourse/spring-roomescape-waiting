package roomescape.service.dto;

import roomescape.domain.Theme;

public record PopularTheme(Theme theme, long reservationCount) {
}
