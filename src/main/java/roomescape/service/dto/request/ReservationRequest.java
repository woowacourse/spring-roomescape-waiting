package roomescape.service.dto.request;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;
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

    private void validate(Object... values) {
        if (Stream.of(values).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException();
        }
    }

    public Reservation toReservation(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(date, member, reservationTime, theme);
    }
}