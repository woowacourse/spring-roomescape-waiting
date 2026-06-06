package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Session {

    private final Long id;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;

    public Session(Long id, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validate(date, timeSlot, theme);
        this.id = id;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public static Session transientOf(LocalDate date, TimeSlot timeSlot, Theme theme) {
        return new Session(null, date, timeSlot, theme);
    }

    public boolean isPast(LocalDateTime currentDateTime) {
        LocalDateTime sessionDateTime = LocalDateTime.of(this.date, this.timeSlot.getStartAt());
        return sessionDateTime.isBefore(currentDateTime);
    }

    public Session reschedule(LocalDate date, TimeSlot timeSlot, Theme theme) {
        LocalDate patchedDate = Objects.requireNonNullElse(date, this.date);
        TimeSlot patchedTimeSlot = Objects.requireNonNullElse(timeSlot, this.timeSlot);
        Theme patchedTheme = Objects.requireNonNullElse(theme, this.theme);
        return new Session(this.id, patchedDate, patchedTimeSlot, patchedTheme);
    }

    private void validate(LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (date == null || timeSlot == null || theme == null) {
            throw new IllegalArgumentException("필수 세션 정보가 누락되었습니다.");
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
