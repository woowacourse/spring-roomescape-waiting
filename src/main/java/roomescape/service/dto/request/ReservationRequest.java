package roomescape.service.dto.request;

import java.time.LocalDate;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationRequest(LocalDate date, Long timeId, Long themeId) {
    public ReservationRequest {
        validate(date, timeId, themeId);
    }

    public ReservationRequest(AdminReservationRequest request) {
        this(request.getDate(), request.getTimeId(), request.getThemeId());
    }

    private void validate(LocalDate date, Long timeId, Long themeId) {
        if (date == null || timeId == null || themeId == null) {
            throw new IllegalArgumentException();
        }
    }

    public Reservation toReservation(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(date, member, reservationTime, theme);
    }
}