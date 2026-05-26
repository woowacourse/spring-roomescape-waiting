package roomescape.reservation.dto.response;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeSimpleResponse theme
) {
    public static ReservationResponse of(Long id, String name, LocalDate date, TimeResponse time, ThemeSimpleResponse theme) {
        return new ReservationResponse(id, name, date, time, theme);
    }
}