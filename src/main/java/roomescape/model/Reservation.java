package roomescape.model;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime reservationTime;
    private final Theme theme;
    private final Member member;

    public Reservation(Long id, LocalDate date, ReservationTime reservationTime, Theme theme,
                       Member member) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme, Member member, LocalDate today) {
        this(null, date, reservationTime, theme, member);
        validateReservationDateInFuture(today);
    }

    private void validateReservationDateInFuture(LocalDate today) {
        if (!this.date.isAfter(today)) {
            throw new IllegalStateException("과거 및 당일 예약은 불가능합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return this.reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
