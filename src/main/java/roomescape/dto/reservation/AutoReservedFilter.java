package roomescape.dto.reservation;

import java.time.LocalDate;

public record AutoReservedFilter(
        LocalDate date,
        Long themeId,
        Long timeId
) {
    public static AutoReservedFilter from(ReservationResponse response) {
        return new AutoReservedFilter(response.date(), response.theme().id(), response.time().id());
    }
}
