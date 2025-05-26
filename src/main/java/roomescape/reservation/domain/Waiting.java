package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;

@Entity
@Table(name = "waitings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"})
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationSlot reservationSlot;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Waiting(
            final ReservationSlot reservationSlot,
            final Member member,
            final LocalDateTime createdAt
    ) {
        validateReservationSlot(reservationSlot);
        validateMember(member);
        validateCreatedAt(createdAt);

        this.id = id;
        this.reservationSlot = reservationSlot;
        this.member = member;
        this.createdAt = createdAt;
    }

    public static Waiting of(
            final ReservationSlot reservationSlot,
            final Member member,
            final LocalDateTime createdAt
    ) {
        return new Waiting(reservationSlot, member, createdAt);
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

    private void validateCreatedAt(final LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시간이 null이면 안됩니다.");
        }
    }
}
