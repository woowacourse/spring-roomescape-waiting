package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

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

    public Waiting() {
    }

    public Waiting(final Long id, final Member member, final Theme theme, final LocalDate date,
                   final ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
    }

    public Waiting(final Member member, final Theme theme, final LocalDate date,
                   final ReservationTime reservationTime) {
        this.id = null;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
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

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
