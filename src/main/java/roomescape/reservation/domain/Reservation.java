package roomescape.reservation.domain;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Getter
@EqualsAndHashCode(of = {"id"})
@ToString
public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Member member;

    public Reservation(final Long id, final LocalDate date, final ReservationTime time,
                       final Theme theme, final Member member) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(final LocalDate date, final ReservationTime time, final Theme theme, final Member member) {
        this(null, date, time, theme, member);
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("멤버는 null이면 안됩니다.");
        }
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null이면 안됩니다.");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null이면 안됩니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 null이면 안됩니다.");
        }
    }
}
