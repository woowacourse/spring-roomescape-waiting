package roomescape.reservation.domain;


import jakarta.persistence.*;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    protected Reservation() {
    }

    public Reservation(Long id, LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.date, reservation.reservationTime, reservation.theme, reservation.member);
    }

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this(null, date, reservationTime, theme, member);
    }

    public boolean isBeforeNow() {
        return reservationTime.isBeforeNow(date);
    }

    public boolean isBetweenInclusive(LocalDate dateFrom, LocalDate dateTo) {
        return !date.isBefore(dateFrom) && !date.isAfter(dateTo);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDate(DateTimeFormatter formatter) {
        return date.format(formatter);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
