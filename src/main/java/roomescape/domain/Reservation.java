package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
    @JoinColumn(name = "time_id")
    private TimeSlot timeSlot;
    @ManyToOne
    private Theme theme;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.RESERVED;

    private Reservation(final Long id, final User user, final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    protected Reservation() {
    }

    public static Reservation ofExisting(final long id, final User user, final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        return new Reservation(id, user, date, timeSlot, theme);
    }

    public static Reservation reserveNewly(final User user, final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        if (isBeforeNow(date, timeSlot)) {
            throw new IllegalArgumentException("이전 날짜로 예약할 수 없습니다.");
        }
        return new Reservation(null, user, date, timeSlot, theme);
    }

    private static boolean isBeforeNow(final LocalDate date, final TimeSlot timeSlot) {
        var now = LocalDateTime.now();
        var today = now.toLocalDate();
        var timeNow = now.toLocalTime();
        return date.isBefore(today)
                || (date.isEqual(today) && timeSlot.isTimeBefore(timeNow));
    }

    public Reservation withId(final long id) {
        if (this.id == null) {
            this.id = id;
            return this;
        }
        throw new IllegalStateException("예약 ID는 재할당할 수 없습니다. 현재 ID: " + this.id);
    }

    public boolean isDateEquals(final LocalDate date) {
        return this.date.isEqual(date);
    }

    public boolean isTimeSlotEquals(final TimeSlot timeSlot) {
        return this.timeSlot.isSameAs(timeSlot);
    }
}

