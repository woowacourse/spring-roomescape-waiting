package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import lombok.ToString;
import roomescape.member.domain.Member;

@Entity
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_slot_id", nullable = false)
    private ReservationSlot reservationSlot;

    public Reservation(final Long id, final BookingStatus status, final Member member,
                       final ReservationSlot reservationSlot) {
        validateMember(member);
        validateReservationSlot(reservationSlot);
        this.id = id;
        this.status = status;
        this.member = member;
        this.reservationSlot = reservationSlot;
    }

    public Reservation(final Member member, final ReservationSlot reservationSlot) {
        this(null, BookingStatus.WAITING, member, reservationSlot);
    }

    public void confirmReservation() {
        if (this.status == BookingStatus.RESERVED) {
            throw new IllegalStateException("예약이 대기 중인 경우에만 확정할 수 있습니다.");
        }

        this.status = BookingStatus.RESERVED;
    }

    private void validateReservationSlot(final ReservationSlot reservationSlot) {
        if (reservationSlot == null) {
            throw new IllegalArgumentException("예약 정보는 null이면 안됩니다.");
        }
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("멤버는 null이면 안됩니다.");
        }
    }
}
