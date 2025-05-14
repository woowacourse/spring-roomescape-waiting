package roomescape.domain.reservation.application.dto.request;

import roomescape.domain.reservation.model.entity.ReservationTheme;

public record CreateReservationThemeServiceRequest(
        String name,
        String description,
        String thumbnail
) {

    public ReservationTheme toReservationTheme() {
        return new ReservationTheme(name, description, thumbnail);
    }
}
