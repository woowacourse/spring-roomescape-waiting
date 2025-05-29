package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.common.exception.ReservationException;
import roomescape.member.domain.Member;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded
    private ReservationSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    public Reservation(Member member, ReservationSlot slot) {
        validateMember(member);
        validateSlot(slot);
        this.member = member;
        this.slot = slot;
        this.status = ReservationStatus.CONFIRMED;
    }

    public Reservation(Member member, ReservationSlot slot, ReservationStatus status) {
        validateMember(member);
        validateSlot(slot);
        this.member = member;
        this.slot = slot;
        this.status = status;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new ReservationException("Member cannot be null");
        }
    }

    private void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new ReservationException("Slot cannot be null");
        }
    }

    public void changeToWaiting() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new ReservationException("확정된 예약만 대기 상태로 변경할 수 있습니다.");
        }
        this.status = ReservationStatus.WAITING;
    }

    public void confirmReservation() {
        if (status != ReservationStatus.WAITING) {
            throw new ReservationException("대기 상태의 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    public String getStatusValue() {
        return status.getDescription();
    }

    public void cancelReservation() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("승인 상태의 예약이 아닙니다.");
        }
        this.status = ReservationStatus.CANCELED;
    }

    public void cancelWaiting() {
        if (status != ReservationStatus.WAITING) {
            throw new IllegalStateException("대기 상태의 예약이 아닙니다.");
        }
        this.status = ReservationStatus.WAITING_CANCELED;
    }

    public boolean isOwnedBy(Member member) {
        return this.member.equals(member);
    }
}
