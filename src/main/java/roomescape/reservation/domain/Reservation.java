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
    private ReservationStatus reservationStatus;

    public Reservation() {
    }

    public Reservation(final Long id, final Member member, final Theme theme, final LocalDate date,
                       final ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
        this.reservationStatus = ReservationStatus.RESERVED;
    }

    public Reservation(final Member member, final Theme theme, final LocalDate date,
                       final ReservationTime reservationTime) {
        this(null, member, theme, date, reservationTime);
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

    public ReservationStatus getStatus() {
        return reservationStatus;
    }
}
