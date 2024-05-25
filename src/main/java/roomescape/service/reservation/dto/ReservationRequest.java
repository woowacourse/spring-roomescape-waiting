package roomescape.service.reservation.dto;

import java.time.DateTimeException;
import java.time.LocalDate;
import roomescape.controller.reservation.dto.AdminReservationRequest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.common.InvalidRequestBodyException;

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

    public Reservation toReservation(ReservationTime reservationTime, Theme theme, Member member) {
        return new Reservation(date, reservationTime, theme, member);
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
