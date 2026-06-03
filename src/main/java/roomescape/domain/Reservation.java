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

    public Reservation(Long id, String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateNullOrBlank(name, date, timeSlot, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public Reservation(String name, LocalDate date, TimeSlot timeSlot, Theme theme,
                                     LocalDateTime now) {
        this(null, name, date, timeSlot, theme);
        validateCreatable(date, timeSlot, now);
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

    public Reservation updateDateAndTime(LocalDate updateDate, TimeSlot updateTime,
                                         LocalDateTime now) {

        validateNotPast(this.date, this.timeSlot, now, "이미 지난 예약은 수정할 수 없습니다.");
        validateNotPast(updateDate, updateTime, now, "이미 지난 날짜로 예약을 수정할 수 없습니다.");

        if (isSameDateAndTime(updateDate, updateTime)) {
            return this;
        }
        return new Reservation(this.id, this.name, updateDate, updateTime, this.theme);
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

    private void validateCreatable(LocalDate date, TimeSlot time, LocalDateTime now) {
        validateNotPast(date, time, now, "지난 날짜/시간으로 예약하실 수 없습니다.");
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

    private void validateNullOrBlank(String name, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
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
}
