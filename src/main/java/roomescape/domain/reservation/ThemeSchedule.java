package roomescape.domain.reservation;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.BusinessRuleViolationException;

@Embeddable
public record ThemeSchedule(
        LocalDate date,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "time_id")
        ReservationTime time,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "theme_id")
        Theme theme
) {
    public void validateReservable(LocalDateTime currentDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new BusinessRuleViolationException("지난 날짜와 시간에 대한 예약은 불가능합니다.");
        }
        Duration duration = Duration.between(currentDateTime, reservationDateTime);
        if (duration.toMinutes() < 10) {
            throw new BusinessRuleViolationException("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
        }
    }
}
