package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;


public class PopularTheme {

    private final Theme theme;
    private final long reservationCount;

    public PopularTheme(Theme theme, long reservationCount) {
        validate(theme, reservationCount);
        this.theme = theme;
        this.reservationCount = reservationCount;
    }

    private void validate(Theme theme, long reservationCount) {
        if (theme == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "테마는 필수입니다.");
        }
        if (reservationCount < 0) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약 수는 음수일 수 없습니다.");
        }
    }

    public Theme getTheme() {
        return theme;
    }

    public long getReservationCount() {
        return reservationCount;
    }
}
