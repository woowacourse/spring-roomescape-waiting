package roomescape.reservation.model.entity;

import static roomescape.reservation.model.entity.vo.ReservationStatus.*;

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
import roomescape.global.exception.AuthorizationException;
import roomescape.member.model.Member;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.entity.vo.ReservationStatus;
import roomescape.reservation.model.vo.Schedule;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

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
    private Reservation(LocalDate date, ReservationStatus status, ReservationTime time, ReservationTheme theme, Member member) {
        this.date = date;
        this.status = status;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static Reservation confirmedFromWaiting(ReservationWaiting waiting) {
        return Reservation.builder()
                .date(waiting.getDate())
                .status(CONFIRMED)
                .time(waiting.getTime())
                .theme(waiting.getTheme())
                .member(waiting.getMember())
                .build();
    }

    public static Reservation createFuture(ReservationDetails details) {
        LocalDateTime requestedDateTime = LocalDateTime.of(details.date(), details.reservationTime().getStartAt());
        validateFutureTime(requestedDateTime);

        return Reservation.builder()
                .date(details.date())
                .status(CONFIRMED)
                .time(details.reservationTime())
                .theme(details.reservationTheme())
                .member(details.member())
                .build();
    }

    public void changeToCancel() {
        this.status = CANCELED;
    }

    public Schedule getSchedule() {
        return Schedule.builder()
                .date(date)
                .timeId(time.getId())
                .themeId(theme.getId())
                .build();
    }

    private static void validateFutureTime(LocalDateTime requestedDateTime) {
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationTimeException("예약시간이 과거시간이 될 수 없습니다. 미래시간으로 입력해주세요.");
        }
    }

    public void checkOwner(Long memberId) {
        if (!Objects.equals(member.getId(), memberId)) {
            throw new AuthorizationException("해당 예약을 취소할 권한이 없습니다.");
        }
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
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
