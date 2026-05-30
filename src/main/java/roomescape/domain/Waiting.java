package roomescape.domain;

import roomescape.exception.InvalidOwnershipException;

import java.time.LocalDate;

public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;
    private final Integer waitingNumber;

    public Waiting(Long id, String name, LocalDate date, TimeSlot timeSlot, Theme theme, Integer waitingNumber) {
        validateFields(name, date, timeSlot, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
        this.waitingNumber = waitingNumber;
    }

    public static Waiting transientOf(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        return new Waiting(null, name, date, timeSlot, theme, null);
    }

    private void validateFields(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약 대기자의 이름은 필수이며 비어있을 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 대기 날짜는 필수입니다.");
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

    public void validateModifiable(String requesterName) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
        }
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

    public Integer getWaitingNumber() {
        return waitingNumber;
    }
}
