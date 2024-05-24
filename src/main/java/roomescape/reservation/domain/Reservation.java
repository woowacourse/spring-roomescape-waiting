package roomescape.reservation.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationSlot reservationSlot;

    private LocalDateTime createdTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(Member member,
                       ReservationSlot reservationSlot,
                       LocalDateTime createdTime,
                       ReservationStatus status) {
        this.member = member;
        this.reservationSlot = reservationSlot;
        this.createdTime = createdTime;
        this.status = status;
    }

    public Reservation(Long id, Member member, ReservationSlot reservationSlot) {
        this.id = id;
        this.member = member;
        this.reservationSlot = reservationSlot;
    }

    public Reservation(Member member, ReservationSlot reservationSlot) {
        this.member = member;
        this.reservationSlot = reservationSlot;
        this.createdTime = LocalDateTime.now();
        this.status = ReservationStatus.BOOKED;
    }

    public boolean isMember(Member member) {
        return this.member.equals(member);
    }

    public void confirmReservation() {
        this.status = ReservationStatus.BOOKED;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationSlot getReservationSlot() {
        return reservationSlot;
    }

    public ReservationStatus getStatus() {
        return status;
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
                ", reservationSlot=" + reservationSlot +
                '}';
    }
}
