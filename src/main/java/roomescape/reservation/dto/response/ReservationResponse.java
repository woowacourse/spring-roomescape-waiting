package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeSimpleResponse theme,
        ReservationStatus status
) {
    public static ReservationResponse of(Long id, String name, LocalDate date, TimeResponse time, ThemeSimpleResponse theme, ReservationStatus status) {
        return new ReservationResponse(id, name, date, time, theme, status);
    }
}