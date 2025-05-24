package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import roomescape.global.common.TimeStamp;
import roomescape.member.domain.Member;

@Entity
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "UPDATE reservation SET deleted_at = NOW() WHERE id = ?")
public class Reservation extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Member member;

    @Embedded
    private ReservationInfo reservationInfo;

    public Reservation() {
    }

    public Reservation(final Long id, final Member member, final Theme theme, final LocalDate date,
                       final ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.reservationInfo = new ReservationInfo(
                theme, date, reservationTime
        );
    }

    public Reservation(final Member member, final Theme theme, final LocalDate date,
                       final ReservationTime reservationTime) {
        this(null, member, theme, date, reservationTime);
    }

    public Reservation(final Member member, final ReservationInfo reservationInfo) {
        this.id = null;
        this.member = member;
        this.reservationInfo = reservationInfo;
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
