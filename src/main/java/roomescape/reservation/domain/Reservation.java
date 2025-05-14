package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import roomescape.member.domain.Member;

public class Reservation {

    private static final long EMPTY_ID = 0L;

    private final Long id;
    private final Member member;
    private final LocalDate reservationDate;
    private final ReservationTime reservationTime;
    private final Theme theme;

    public Reservation(
            final Long id,
            final Member member,
            final LocalDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        this.id = id;
        this.member = member;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.theme = theme;
        validateNull();
    }

    public static Reservation withoutId(
            final Member member,
            final LocalDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        return new Reservation(EMPTY_ID, member, reservationDate, reservationTime, theme);
    }

    private void validateNull() {
        if (member == null || reservationDate == null || reservationTime == null || theme == null) {
            throw new IllegalArgumentException("Reservation field cannot be null");
        }
    }

    public boolean existId() {
        return id != EMPTY_ID;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationTimeId() {
        return reservationTime.getId();
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public LocalTime getReservationStartTime() {
        return reservationTime.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
