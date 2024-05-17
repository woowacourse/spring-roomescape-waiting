package roomescape.reservation.domain;

import jakarta.persistence.*;

import java.util.Objects;
import roomescape.member.domain.Member;

@Entity
public class MemberReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    @ManyToOne
    private Reservation reservation;

    public MemberReservation() {
    }

    public MemberReservation(Long id, Member member, Reservation reservation) {
        this.id = id;
        this.member = member;
        this.reservation = reservation;
    }

    public MemberReservation(Member member, Reservation reservation) {
        this.member = member;
        this.reservation = reservation;
    }

    public boolean isMember(Member member) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberReservation that = (MemberReservation) o;
        return Objects.equals(getId(), that.getId());
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
                '}';
    }
}
