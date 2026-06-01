package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastTimeException;

public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;
    private final Integer waitingNumber;

    public Waiting(Long id, String name, LocalDate date, TimeSlot timeSlot, Theme theme, Integer waitingNumber) {
        validateNullOrBlank(name, date, timeSlot, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
        this.waitingNumber = waitingNumber;
    }

    public Waiting(String name, LocalDate date, TimeSlot timeSlot, Theme theme, LocalDateTime now) {
        this(null, name, date, timeSlot, theme, null);
        validateCreatable(date, timeSlot, now);
    }

    private void validateNullOrBlank(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
    }

    private void validateCreatable(LocalDate date, TimeSlot time, LocalDateTime now) {
        validateNotPast(date, time, now, "지난 날짜/시간으로 예약 대기를 추가할 수 없습니다.");
    }

    public void validateCancelable(LocalDateTime now, String userName) {
        validateNotPast(this.date, this.timeSlot, now, "이미 지난 대기 정보는 삭제할 수 없습니다.");
        validateOwnedBy(userName);
    }

    private void validateNotPast(LocalDate date, TimeSlot timeSlot, LocalDateTime now, String errorMessage) {
        if (isPast(date, timeSlot, now)) {
            throw new PastTimeException(errorMessage);
        }
    }

    private boolean isPast(LocalDate date, TimeSlot timeSlot, LocalDateTime now) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, timeSlot.getStartAt());
        return targetDateTime.isBefore(now);
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

    private void validateOwnedBy(String userName) {
        if (!this.name.equals(userName)) {
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
