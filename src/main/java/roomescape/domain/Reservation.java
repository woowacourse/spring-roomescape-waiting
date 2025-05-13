package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private final Long id;
    private final Member member;
    private final Theme theme;
    private final LocalDate reservationDate;
    private final ReservationTime reservationTime;

    private Reservation(Long id, Member member, Theme theme, LocalDate reservationDate,
                        ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
    }

    public static Reservation of(Long id, Member member, Theme theme, LocalDate date, ReservationTime time) {
        return new Reservation(id, member, theme, date, time);
    }

    public static Reservation withoutId(Member member, Theme theme, LocalDate reservationDate,
                                        ReservationTime reservationTime) {
        return new Reservation(null, member, theme, reservationDate, reservationTime);
    }

    public static Reservation assignId(Long id, Reservation reservation) {
        return new Reservation(id, reservation.getMember(), reservation.getTheme(), reservation.getReservationDate(),
                reservation.getReservationTime());
    }

    public boolean isPast() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime.getStartAt());
        return reservationDateTime.isBefore(now);
    }

    public boolean isDuplicated(Reservation other) {
        return this.reservationDate.equals(other.reservationDate)
               && this.reservationTime.equals(other.reservationTime)
               && this.theme.equals(other.theme);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
