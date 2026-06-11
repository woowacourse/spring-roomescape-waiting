package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationSlot {

    private final Long id;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;

    public ReservationSlot(Long id, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateNullOrBlank(date, timeSlot, theme);
        this.id = id;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public ReservationSlot(LocalDate date, TimeSlot timeSlot, Theme theme) {
        this(null, date, timeSlot, theme);
    }

    public boolean isPast(LocalDateTime baseTime) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, timeSlot.getStartAt());
        return targetDateTime.isBefore(baseTime);
    }

    public boolean isSameDateAndTime(LocalDate updateDate, TimeSlot updateTime) {
        return this.date.equals(updateDate) && this.timeSlot.equals(updateTime);
    }

    public boolean isSameSlot(ReservationSlot other) {
        return other != null
                && this.date.equals(other.date)
                && this.timeSlot.getId().equals(other.timeSlot.getId())
                && this.theme.getId().equals(other.theme.getId());
    }

    private void validateNullOrBlank(LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
    }


    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 필수입니다.");
        }
    }

    private void validateTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Theme getTheme() {
        return theme;
    }
}
