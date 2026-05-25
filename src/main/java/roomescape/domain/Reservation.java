package roomescape.domain;

import roomescape.exception.InvalidOwnershipException;

import java.time.LocalDate;
import java.util.Objects;

public class Reservation {

    private final Long id;
    private String name;
    private LocalDate date;
    private TimeSlot timeSlot;
    private Theme theme;

    public Reservation(Long id, String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateFields(name, date, timeSlot, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public static Reservation transientOf(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        return new Reservation(null, name, date, timeSlot, theme);
    }

    public void validateModifiable(String requesterName) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
        }
    }

    public void reschedule(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        String patchedName = Objects.requireNonNullElse(name, this.name);
        LocalDate patchedDate = Objects.requireNonNullElse(date, this.date);
        TimeSlot patchedTime = Objects.requireNonNullElse(timeSlot, this.timeSlot);
        Theme patchedTheme = Objects.requireNonNullElse(theme, this.theme);

        validateFields(patchedName, patchedDate, patchedTime, patchedTheme);
        this.name = patchedName;
        this.date = patchedDate;
        this.timeSlot = patchedTime;
        this.theme = patchedTheme;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

    private void validateFields(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수이며 비어있을 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 필수입니다.");
        }
    }

    private void validateTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("존재하지 않는 예약 시간대입니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("존재하지 않는 테마입니다.");
        }
    }
}
