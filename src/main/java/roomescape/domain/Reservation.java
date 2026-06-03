package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.PastTimeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;
    private final LocalDateTime createdAt;

    public Reservation(Long id, String name, LocalDate date, TimeSlot timeSlot, Theme theme,
                       LocalDateTime createdAt) {
        validateNullOrBlank(name, date, timeSlot, theme, createdAt);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
        this.createdAt = createdAt;
        validateCreatable();
    }

    public Reservation(String name, LocalDate date, TimeSlot timeSlot, Theme theme,
                       LocalDateTime createdAt) {
        this(null, name, date, timeSlot, theme, createdAt);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Reservation updateDateAndTime(LocalDate updateDate, TimeSlot updateTime,
                                         LocalDateTime now) {

        validateNotPast(this.date, this.timeSlot, now, "이미 지난 예약은 수정할 수 없습니다.");
        validateNotPast(updateDate, updateTime, now, "이미 지난 날짜로 예약을 수정할 수 없습니다.");

        if (isSameDateAndTime(updateDate, updateTime)) {
            return this;
        }
        return new Reservation(this.id, this.name, updateDate, updateTime, this.theme, this.createdAt);
    }

    public void validateCancelable(LocalDateTime now) {
        validateNotPast(this.date, this.timeSlot, now, "이미 지난 예약은 삭제할 수 없습니다.");
    }

    public boolean hasSameDateAndTime(Reservation other) {
        return isSameDateAndTime(other.date, other.timeSlot);
    }

    public boolean isOwner(String requestName) {
        return name.equals(requestName);
    }

    private boolean isSameDateAndTime(LocalDate updateDate, TimeSlot updateTime) {
        return this.date.equals(updateDate) && this.timeSlot.equals(updateTime);
    }

    private void validateCreatable() {
        validateNotPast(this.date, this.timeSlot, this.createdAt, "지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    private void validateNotPast(LocalDate date, TimeSlot timeSlot, LocalDateTime baseTime, String errorMessage) {
        if (isPast(date, timeSlot, baseTime)) {
            throw new PastTimeException(errorMessage);
        }
    }

    private boolean isPast(LocalDate date, TimeSlot timeSlot, LocalDateTime baseTime) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, timeSlot.getStartAt());
        return targetDateTime.isBefore(baseTime);
    }

    private void validateNullOrBlank(String name, LocalDate date, TimeSlot timeSlot, Theme theme,
                                     LocalDateTime createdAt) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
        validateCreatedAt(createdAt);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }
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

    private void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("예약 생성 시각은 필수입니다.");
        }
    }
}
