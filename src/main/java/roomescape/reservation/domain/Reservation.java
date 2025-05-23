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
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

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
    private ReservationStatus status;

    public Reservation() {
    }

    private Reservation(final Long id, final Member member, final Theme theme, final LocalDate date,
                        final ReservationTime reservationTime, final ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    public static Reservation createReserved(final Member member, final Theme theme, final LocalDate date,
                                             final ReservationTime reservationTime) {
        return new Reservation(null, member, theme, date, reservationTime, ReservationStatus.RESERVED);
    }

    public static Reservation createWaiting(final Member member, final Theme theme, final LocalDate date,
                                            final ReservationTime reservationTime) {
        return new Reservation(null, member, theme, date, reservationTime, ReservationStatus.WAITING);
    }

    public void acceptStatus() {
        status = ReservationStatus.RESERVED;
    }

    public boolean isReserved() {
        return status == ReservationStatus.RESERVED;
    }

    public String getReservationStatus() {
        return status.getStatus();
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
        return status;
    }
}
