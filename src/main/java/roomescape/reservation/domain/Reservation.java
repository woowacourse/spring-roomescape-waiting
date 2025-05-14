package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.InvalidReservationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                        final Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation of(final Long id, final LocalDate date, final Member member, final ReservationTime time,
                                 final Theme theme) {
        return new Reservation(id, member, date, time, theme);
    }

    public static Reservation createUpcomingReservationWithUnassignedId(final Member member, final LocalDate date,
                                                                        final ReservationTime time,
                                                                        final Theme theme, final LocalDateTime now) {
        validateDateTime(date, time.getStartAt(), now);
        return new Reservation(null, member, date, time, theme);
    }

    private static void validateDateTime(LocalDate date, LocalTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new InvalidReservationException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final Reservation that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMember(), that.getMember())
                && Objects.equals(getDate(), that.getDate()) && Objects.equals(getTime(),
                that.getTime()) && Objects.equals(getTheme(), that.getTheme());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMember(), getDate(), getTime(), getTheme());
    }
}
