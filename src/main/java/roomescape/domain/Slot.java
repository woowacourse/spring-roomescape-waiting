package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;

    public Slot(Long id, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validate(date, timeSlot, theme);
        this.id = id;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public static Slot transientOf(LocalDate date, TimeSlot timeSlot, Theme theme) {
        return new Slot(null, date, timeSlot, theme);
    }

    public boolean isPast(LocalDateTime currentDateTime) {
        LocalDateTime slotDateTime = LocalDateTime.of(this.date, this.timeSlot.getStartAt());
        return slotDateTime.isBefore(currentDateTime);
    }

    public Slot reschedule(LocalDate date, TimeSlot timeSlot, Theme theme) {
        LocalDate patchedDate = Objects.requireNonNullElse(date, this.date);
        TimeSlot patchedTimeSlot = Objects.requireNonNullElse(timeSlot, this.timeSlot);
        Theme patchedTheme = Objects.requireNonNullElse(theme, this.theme);
        return new Slot(this.id, patchedDate, patchedTimeSlot, patchedTheme);
    }

    private void validate(LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (date == null || timeSlot == null || theme == null) {
            throw new IllegalArgumentException("필수 슬롯 정보가 누락되었습니다.");
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
