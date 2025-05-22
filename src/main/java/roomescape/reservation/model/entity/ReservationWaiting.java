package roomescape.reservation.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import roomescape.member.model.Member;
import roomescape.reservation.model.dto.ReservationWaitingDetails;
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
    public ReservationWaiting(LocalDate date, ReservationTime time, ReservationTheme theme, Member member) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static ReservationWaiting createFuture(ReservationWaitingDetails details) {
        LocalDateTime requestedDateTime = LocalDateTime.of(details.date(), details.reservationTime().getStartAt());
        validateFutureTime(requestedDateTime);
        return details.toReservationWaiting();
    }

    private static void validateFutureTime(LocalDateTime requestedDateTime) {
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationTimeException("예약시간이 과거시간이 될 수 없습니다. 미래시간으로 입력해주세요.");
        }
    }
}
