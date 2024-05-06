package roomescape.controller.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank
        String name,
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId) {

    public Reservation toDomain(final ReservationTime time, final Theme theme) {
        return new Reservation(null, name, date, time, theme);
    }
}
