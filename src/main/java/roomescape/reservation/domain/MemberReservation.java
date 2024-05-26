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
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;

// TODO: Reservation 으로 변경 고려
@Entity
public class MemberReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationDetail reservationDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public MemberReservation() {
    }

    public MemberReservation(final ReservationDetail reservationDetail, final Member member, final ReservationStatus status) {
        this.reservationDetail = reservationDetail;
        this.member = member;
        this.status = status;
        validateNotNull();
    }

    private void validateNotNull() {
        if (reservationDetail == null || member == null || status == null) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "예약 대기(MemberReservation) 생성 값(reservationDetail, member, status)에 null이 입력되었습니다.");
        }
    }

    public boolean isReservedStatus() {
        return status.isReserved();
    }

    public boolean isWaitingStatus() {
        return status.isWaiting();
    }

    public Long getId() {
        return id;
    }

    public ReservationDetail getReservationDetail() {
        return reservationDetail;
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void changeStatusToReserve() {
        this.status = ReservationStatus.RESERVED;
    }

    @Override
    public String toString() {
        return "MemberReservation{" +
                "id=" + id +
                ", reservationDetail=" + reservationDetail +
                ", member=" + member +
                ", status=" + status +
                '}';
    }
}
