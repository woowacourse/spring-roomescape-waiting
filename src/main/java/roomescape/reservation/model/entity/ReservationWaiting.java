package roomescape.reservation.model.entity;

import static roomescape.reservation.model.entity.vo.ReservationWaitingStatus.*;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import roomescape.global.exception.AuthorizationException;
import roomescape.member.model.Member;
import roomescape.reservation.model.dto.ReservationWaitingDetails;
import roomescape.reservation.model.entity.vo.ReservationWaitingStatus;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationWaitingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private ReservationTheme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    private ReservationWaiting(LocalDate date, ReservationWaitingStatus status, ReservationTime time,
            ReservationTheme theme, Member member) {
        this.date = date;
        this.status = status;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static ReservationWaiting createFuture(ReservationWaitingDetails details) {
        LocalDateTime requestedDateTime = LocalDateTime.of(details.date(), details.reservationTime().getStartAt());
        validateFutureTime(requestedDateTime);
        return ReservationWaiting.builder()
                .date(details.date())
                .status(PENDING)
                .time(details.reservationTime())
                .theme(details.reservationTheme())
                .member(details.member())
                .build();
    }

    public void changeToAccept() {
        this.status = ACCEPTED;
    }

    public void changeToCancel() {
        this.status = CANCELED;
    }

    public void checkOwner(Long memberId) {
        if (!Objects.equals(member.getId(), memberId)) {
            throw new AuthorizationException("해당 웨이팅을 취소할 권한이 없습니다.");
        }
    }

    private static void validateFutureTime(LocalDateTime requestedDateTime) {
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationTimeException("예약시간이 과거시간이 될 수 없습니다. 미래시간으로 입력해주세요.");
        }
    }
}
