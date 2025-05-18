package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.BusinessRuleViolationException;

@Embeddable
@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
@ToString
public class ReservationDateTime {

    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private TimeSlot timeSlot;

    private ReservationDateTime(final LocalDate date, final TimeSlot timeSlot) {
        this.date = date;
        this.timeSlot = timeSlot;
    }

    protected ReservationDateTime() {

    }

    public static ReservationDateTime forReserve(final LocalDate date, final TimeSlot timeSlot) {
        if (isBeforeNow(date, timeSlot)) {
            throw new BusinessRuleViolationException("지나간 일시로 예약할 수 없습니다.");
        }
        return new ReservationDateTime(date, timeSlot);
    }

    private static boolean isBeforeNow(final LocalDate date, final TimeSlot timeSlot) {
        var now = LocalDateTime.now();
        var today = now.toLocalDate();
        var timeNow = now.toLocalTime();
        return date.isBefore(today)
               || (date.isEqual(today) && timeSlot.isTimeBefore(timeNow));
    }
}
