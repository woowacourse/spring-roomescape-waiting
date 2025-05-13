package roomescape.reservation.business.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.business.domain.Member;
import roomescape.theme.business.domain.Theme;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Member member;

    public Reservation(final Long id, final LocalDate date, final ReservationTime time, final Theme theme,
                       final Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(final LocalDate date, final ReservationTime time, final Theme theme, final Member member) {
        this(null, date, time, theme, member);
    }

    public boolean hasConflictWith(final ReservationTime reservationTime, final Theme theme) {
        final LocalTime startAt = time.getStartAt();
        return reservationTime.hasConflict(theme.getDuration(), startAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
