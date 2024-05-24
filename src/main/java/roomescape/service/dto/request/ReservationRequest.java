package roomescape.service.dto.request;

import java.time.DateTimeException;
import java.time.LocalDate;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class ReservationRequest {
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public ReservationRequest(String date, String timeId, String themeId) {
        validate(date, timeId, themeId);
        this.date = LocalDate.parse(date);
        this.timeId = Long.parseLong(timeId);
        this.themeId = Long.parseLong(themeId);
    }

    public ReservationRequest(AdminReservationRequest request) {
        this.date = request.getDate();
        this.timeId = request.getTimeId();
        this.themeId = request.getThemeId();
    }

    private void validate(String date, String timeId, String themeId) {
        if (date == null || timeId == null || themeId == null) {
            throw new IllegalArgumentException();
        }
        try {
            LocalDate.parse(date);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException();
        }
    }

    public Reservation toReservation(Member member, ReservationTime reservationTime, Theme theme,
                                     ReservationStatus status) {
        return new Reservation(date, member, reservationTime, theme, status);
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
