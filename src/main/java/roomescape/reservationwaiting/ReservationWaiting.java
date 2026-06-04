package roomescape.reservationwaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;

public class ReservationWaiting {
    private final Long id;
    private final LocalDate date;
    private final Long themeId;
    private final Long timeId;
    private final String name;
    private final LocalDateTime requestAt;

    public ReservationWaiting(Long id, LocalDate date, Long themeId, Long timeId, String name, LocalDateTime requestAt) {
        this.id = id;
        this.date = date;
        this.themeId = themeId;
        this.timeId = timeId;
        this.name = validateName(name);
        this.requestAt = requestAt;
    }

    public static ReservationWaiting createNew(LocalDate date, Long themeId, Long timeId, String name, LocalDateTime requestAt) {
        return new ReservationWaiting(null, date, themeId, timeId, name, requestAt);
    }

    public ReservationWaiting withId(final Long id) {
        return new ReservationWaiting(id, this.date, this.themeId, this.timeId, this.name, this.requestAt);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() { return date; }

    public Long getThemeId() { return themeId; }

    public Long getTimeId() { return timeId; }

    public String getName() {
        return name;
    }

    public LocalDateTime getRequestAt() {
        return requestAt;
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 필수입니다.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() >= 10) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 10자 미만이어야 합니다.");
        }

        return trimmedName;
    }
}
