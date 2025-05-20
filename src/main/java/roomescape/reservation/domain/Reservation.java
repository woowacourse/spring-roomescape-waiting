package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Member member;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    @JoinColumn(nullable = false)
    @ManyToOne
    private ReservationTime reservationTime;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status;

    public Reservation() {
    }

    public Reservation(final Long id, final Member member, final Theme theme, final LocalDate date,
                       final ReservationTime reservationTime, final Status status) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    public static Reservation createReserved(final Member member, final Theme theme, final LocalDate date,
                                             final ReservationTime reservationTime) {
        return new Reservation(null, member, theme, date, reservationTime, Status.RESERVED);
    }

    public static Reservation createWaiting(final Member member, final Theme theme, final LocalDate date,
                                            final ReservationTime reservationTime) {
        return new Reservation(null, member, theme, date, reservationTime, Status.WAITING);
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

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }
}
