package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.exception.reservation.CancelReservationException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationDetail detail;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Reservation(Member member, ReservationDetail detail, Status status) {
        this(null, member, detail, status, null);
    }

    public Reservation approve() {
        if (this.isCanceled()) {
            throw new CancelReservationException("이미 취소된 예약입니다.");
        }
        this.status = Status.RESERVED;
        return this;
    }

    public void cancel(Long memberId) {
        if (this.isNotOwner(memberId)) {
            throw new CancelReservationException("다른 회원의 예약을 취소할 수 없습니다.");
        }
        if (this.isReserved()) {
            throw new CancelReservationException("예약 취소는 어드민만 할 수 있습니다.");
        }
        forceCancel();
    }

    public void forceCancel() {
        if (this.isCanceled()) {
            throw new CancelReservationException("이미 취소된 예약입니다.");
        }
        this.status = Status.CANCELED;
    }

    public boolean isNotOwner(Long id) {
        return !this.member.getId().equals(id);
    }

    public boolean isReserved() {
        return this.status == Status.RESERVED;
    }

    public boolean isWaiting() {
        return this.status == Status.WAITING;
    }

    private boolean isCanceled() {
        return this.status == Status.CANCELED;
    }
}
