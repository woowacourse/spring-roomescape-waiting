package roomescape.domain;

import roomescape.exception.InvalidDomainException;

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
            throw new InvalidDomainException("테마는 필수입니다.");
        }
        if (reservationCount < 0) {
            throw new InvalidDomainException("예약 수는 음수일 수 없습니다.");
        }
    }

    public Theme getTheme() {
        return theme;
    }

    public long getReservationCount() {
        return reservationCount;
    }
}
