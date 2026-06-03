package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.PastTimeException;

public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final Theme theme;
    private final LocalDateTime createdAt;

    public Waiting(Long id, String name, LocalDate date, TimeSlot timeSlot, Theme theme, LocalDateTime createdAt) {
        validateNullOrBlank(name, date, timeSlot, theme, createdAt);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public Waiting(String name, LocalDate date, TimeSlot timeSlot, Theme theme, LocalDateTime createdAt) {
        this(null, name, date, timeSlot, theme, createdAt);
        validateCreatable(date, timeSlot, createdAt);
    }

    private void validateNullOrBlank(String name, LocalDate date, TimeSlot timeSlot, Theme theme,
                                     LocalDateTime createdAt) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlot);
        validateTheme(theme);
        validateCreatedAt(createdAt);
    }

    private void validateCreatable(LocalDate date, TimeSlot time, LocalDateTime now) {
        validateNotPast(date, time, now, "지난 날짜/시간으로 예약 대기를 추가할 수 없습니다.");
    }

    public void validateCancelable(LocalDateTime now) {
        validateNotPast(this.date, this.timeSlot, now, "이미 지난 대기 정보는 삭제할 수 없습니다.");
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
            throw new IllegalArgumentException("예약 대기자의 이름은 필수입니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 대기 날짜는 필수입니다.");
        }
    }

    private void validateTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("예약 대기 시간은 필수입니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
    }

    private void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("예약 대기 생성 시각은 필수입니다.");
        }
    }

    public boolean isOwner(String requestName) {
        return name.equals(requestName);
    }

    public boolean hasSameSlot(Waiting other) {
        return date.equals(other.date)
                && timeSlot.getId().equals(other.timeSlot.getId())
                && theme.getId().equals(other.theme.getId());
    }

    public boolean isSameWaiting(Waiting target) {
        return id != null && id.equals(target.id);
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
}
