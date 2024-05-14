package roomescape.reservation.domain;

import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public class Reservation {
    private final Long id;
    private final LocalDate date;
    private final ReservationTime reservationTime;
    private final Theme theme;
    private final Member member;

    public Reservation(final LocalDate date, final ReservationTime reservationTime, final Theme theme, final Member member) {
        this(null, date, reservationTime, theme, member);
    }

    public Reservation(final Long id, final LocalDate date, final ReservationTime reservationTime, final Theme theme, final Member member) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;

        validateBlank();
    }

    private void validateBlank() {
        if (date == null || reservationTime == null || theme == null || member == null) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("예약(Reservation) 생성에 유효하지 않은 값(null OR 공백)이 입력되었습니다. [values: %s]", this));
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
