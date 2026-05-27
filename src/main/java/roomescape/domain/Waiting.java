package roomescape.domain;

import java.time.LocalDate;
import roomescape.exception.InvalidOwnershipException;

public class Waiting {

    private final Long id;
    private String name;
    private LocalDate date;
    private Long timeSlotId;
    private Long themeId;
    private Integer waitingNumber;

    public Waiting(Long id, String name, LocalDate date, Long timeSlotId, Long themeId, Integer waitingNumber) {
        validateFields(name, date, timeSlotId, themeId);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeSlotId = timeSlotId;
        this.themeId = themeId;
        this.waitingNumber = waitingNumber;
    }

    public static Waiting transientOf(String name, LocalDate date, Long timeSlotId, Long themeId) {
        return new Waiting(null, name, date, timeSlotId, themeId, null);
    }

    private void validateFields(String name, LocalDate date, Long timeSlotId, Long themeId) {
        validateName(name);
        validateDate(date);
        validateTimeSlot(timeSlotId);
        validateTheme(themeId);
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

    private void validateTimeSlot(Long timeSlotId) {
        if (timeSlotId == null) {
            throw new IllegalArgumentException("존재하지 않는 예약 시간대입니다.");
        }
    }

    private void validateTheme(Long themeId) {
        if (themeId == null) {
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

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Integer getWaitingNumber() {
        return waitingNumber;
    }
}
