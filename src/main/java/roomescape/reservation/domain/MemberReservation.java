package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import roomescape.global.entity.BaseEntity;
import roomescape.member.domain.Member;

@Entity
public class MemberReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RESERVATION_ID")
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    public MemberReservation() {
    }

    public MemberReservation(Long id, Member member, Reservation reservation, ReservationStatus reservationStatus) {
        this.id = id;
        this.member = member;
        this.reservation = reservation;
        this.reservationStatus = reservationStatus;
    }

    public MemberReservation(Member member, Reservation reservation, ReservationStatus reservationStatus) {
        this(null, member, reservation, reservationStatus);
    }

    public boolean isPending() {
        return this.reservationStatus.equals(ReservationStatus.PENDING);
    }

    public void approve() {
        this.reservationStatus = ReservationStatus.APPROVED;
    }

    public void deny() {
        this.reservationStatus = ReservationStatus.DENY;
    }

    public boolean canDelete(Member member) {
        return member.isAdmin() || isRegisteredMember(member);
    }

    private boolean isRegisteredMember(Member member) {
        return this.member.equals(member);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberReservation)) {
            return false;
        }
        MemberReservation that = (MemberReservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "MemberReservation{" +
                "id=" + id +
                ", member=" + member +
                ", reservation=" + reservation +
                ", reservationStatus=" + reservationStatus +
                '}';
    }
}
