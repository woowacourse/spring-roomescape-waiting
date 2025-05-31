package roomescape.domain.waiting;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.exception.BusinessRuleViolationException;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private LocalDate date;
    @ManyToOne
    private TimeSlot timeSlot;
    @ManyToOne
    private Theme theme;

    private Waiting(final Long id,
                    final User user,
                    final LocalDate date,
                    final TimeSlot timeSlot,
                    final Theme theme) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    protected Waiting() {
    }

    public static Waiting register(final User user,
                                   final LocalDate date,
                                   final TimeSlot timeSlot,
                                   final Theme theme) {

        validateNotPastDateTime(date, timeSlot);
        return new Waiting(null, user, date, timeSlot, theme);
    }

    private static void validateNotPastDateTime(final LocalDate date, final TimeSlot timeSlot) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        boolean isPastDate = date.isBefore(currentDate);
        boolean isCurrentDateAndPastTime = date.isEqual(currentDate) && timeSlot.isTimeBefore(currentTime);

        if (isPastDate || isCurrentDateAndPastTime) {
            throw new BusinessRuleViolationException("이전 날짜로 예약 대기 신청할 수 없습니다.");
        }
    }
}
