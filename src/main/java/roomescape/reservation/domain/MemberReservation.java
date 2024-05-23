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
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;

@Entity
public class MemberReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    @Column(name = "reservation_order", nullable = false)
    private ReservationOrder order;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public MemberReservation() {
    }

    public MemberReservation(final Reservation reservation, final Member member, final ReservationStatus status, final Long order) {
        this.reservation = reservation;
        this.member = member;
        this.status = status;
        this.order = new ReservationOrder(order);

        validateNotNull();
    }

    private void validateNotNull() {
        if (reservation == null || member == null || status == null) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "예약 대기(MemberReservation) 생성에 null이 입력되었습니다.");
        }
    }

    public void increaseOrder() {
        this.order = order.increase();
        if (isPossibleReserve()) {
            changeStatusToReserve();
        }
    }

    public boolean isPossibleReserve() {
        return order.isReservationPossibleOrder();
    }

    public boolean isReserved() {
        return status.isReserved();
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getOrder() {
        return order.reservationOrder();
    }

    public void changeStatusToReserve() {
        this.status = ReservationStatus.RESERVED;
    }
}
