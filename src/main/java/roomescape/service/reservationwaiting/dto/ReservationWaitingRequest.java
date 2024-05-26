package roomescape.service.reservationwaiting.dto;

import java.time.DateTimeException;
import java.time.LocalDate;
import roomescape.exception.common.InvalidRequestBodyException;

public class ReservationWaitingRequest {
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public ReservationWaitingRequest(String date, String timeId, String themeId) {
        validate(date, timeId, themeId);
        this.date = LocalDate.parse(date);
        this.timeId = Long.parseLong(timeId);
        this.themeId = Long.parseLong(themeId);
    }

    public void validate(String date, String timeId, String themeId) {
        if (date == null || date.isBlank() ||
                timeId == null || timeId.isBlank() ||
                themeId == null || themeId.isBlank()) {
            throw new InvalidRequestBodyException();
        }
        try {
            LocalDate.parse(date);
        } catch (DateTimeException e) {
            throw new InvalidRequestBodyException();
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
