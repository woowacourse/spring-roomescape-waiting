package roomescape.domain;

import java.time.LocalDate;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;

    public Slot(Long id, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateFields(date, timeSlot, theme);
        this.id = id;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public static Slot transientOf(LocalDate date, TimeSlot timeSlot, Theme theme) {
        return new Slot(null, date, timeSlot, theme);
    }

    private void validateFields(LocalDate date, TimeSlot timeSlot, Theme theme) {
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
