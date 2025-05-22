package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Reservation {

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
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.RESERVED;

    private Reservation(final Long id,
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

    protected Reservation() {
    }

    public static Reservation ofExisting(final long id,
                                         final User user,
                                         final LocalDate date,
                                         final TimeSlot timeSlot,
                                         final Theme theme) {
        return new Reservation(id, user, date, timeSlot, theme);
    }

    public static Reservation reserveNewly(final User user,
                                           final LocalDate date,
                                           final TimeSlot timeSlot,
                                           final Theme theme) {

        validateNotPastDateTime(date, timeSlot);
        return new Reservation(null, user, date, timeSlot, theme);
    }

    private static void validateNotPastDateTime(final LocalDate date, final TimeSlot timeSlot) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        boolean isPastDate = date.isBefore(currentDate);
        boolean isCurrentDateAndPastTime = date.isEqual(currentDate) && timeSlot.isTimeBefore(currentTime);

        if (isPastDate || isCurrentDateAndPastTime) {
            throw new BusinessRuleViolationException("이전 날짜로 예약할 수 없습니다.");
        }
    }
}

