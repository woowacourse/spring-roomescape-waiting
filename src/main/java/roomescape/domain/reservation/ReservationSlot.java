package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.BusinessRuleViolationException;

@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ReservationSlot {

    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private TimeSlot timeSlot;
    @ManyToOne
    private Theme theme;

    private ReservationSlot(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public static ReservationSlot of(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        return new ReservationSlot(date, timeSlot, theme);
    }

    public static ReservationSlot forReserve(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        if (isBeforeNow(date, timeSlot)) {
            throw new BusinessRuleViolationException("지나간 일시로 예약할 수 없습니다.");
        }
        return new ReservationSlot(date, timeSlot, theme);
    }

    private static boolean isBeforeNow(final LocalDate date, final TimeSlot timeSlot) {
        var now = LocalDateTime.now();
        var today = now.toLocalDate();
        var timeNow = now.toLocalTime();
        return date.isBefore(today)
               || (date.isEqual(today) && timeSlot.isTimeBefore(timeNow));
    }
}
