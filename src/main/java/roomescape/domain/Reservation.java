package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {

    private final Long id;
    private final Member member;
    private final ReservationDate reservationDate;
    private final ReservationTime reservationTime;
    private final Theme theme;

    private Reservation(
            Long id,
            Member member,
            LocalDate reservationDate,
            ReservationTime reservationTime,
            Theme theme
    ) {
        this.id = id;
        this.member = member;
        this.reservationDate = new ReservationDate(reservationDate);
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public static Reservation create(
            Member member,
            LocalDate reservationDate,
            ReservationTime reservationTime,
            Theme theme
    ) {
        return new Reservation(null, member, reservationDate, reservationTime, theme);
    }

    public static Reservation create(
            Long id,
            Member member,
            LocalDate reservationDate,
            ReservationTime reservationTime,
            Theme theme
    ) {
        return new Reservation(id, member, reservationDate, reservationTime, theme);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return reservationDate.getDate();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Long getTimeId() {
        return reservationTime.getId();
    }

    public Theme getTheme() {
        return theme;
    }
}
