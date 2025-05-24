package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;

@Entity
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationSlot reservationSlot;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private Reservation(
            final ReservationSlot reservationSlot,
            final Member member,
            final ReservationStatus status
    ) {
        validateReservationSlot(reservationSlot);
        validateMember(member);
        validateStatus(status);

        this.reservationSlot = reservationSlot;
        this.member = member;
        this.status = status;
    }

    public static Reservation of(
            final ReservationSlot reservationSlot,
            final Member member,
            final ReservationStatus status
    ) {
        return new Reservation(reservationSlot, member, status);
    }

    public void updateMember(final Member member) {
        this.member = member;
    }

    private void validateReservationSlot(final ReservationSlot reservationSlot) {
        if (reservationSlot == null) {
            throw new IllegalArgumentException("예약 슬롯은 null이면 안됩니다.");
        }
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("멤버는 null이면 안됩니다.");
        }
    }

    private void validateStatus(final ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 null이면 안됩니다.");
        }
    }
}
