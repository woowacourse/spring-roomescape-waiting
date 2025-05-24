package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
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

    @Embedded
    private ReservationInfo reservationInfo;

    public Waiting() {
    }

    public Waiting(final Long id, final Member member, final Theme theme, final LocalDate date,
                   final ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.reservationInfo = new ReservationInfo(
                theme, date, reservationTime
        );
    }

    public Waiting(final Member member, final Theme theme, final LocalDate date,
                   final ReservationTime reservationTime) {
        this.id = null;
        this.member = member;
        this.reservationInfo = new ReservationInfo(
                theme, date, reservationTime
        );
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationInfo getReservationInfo() {
        return reservationInfo;
    }
}
