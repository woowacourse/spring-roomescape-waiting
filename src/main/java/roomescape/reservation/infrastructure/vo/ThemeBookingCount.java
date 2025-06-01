package roomescape.reservation.infrastructure.vo;

import roomescape.reservation.domain.BookedCount;
import roomescape.theme.domain.Theme;

public record ThemeBookingCount(Theme theme, BookedCount bookedCount) {

    public ThemeBookingCount(Theme theme, Long bookedCount) {
        this(theme, BookedCount.from(bookedCount));
    }
}
