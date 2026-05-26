package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;

public record CreateReservationResponse(
    Long id,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,
    ThemePayload theme
) {

    public static CreateReservationResponse from(Reservation reservation) {
        return new CreateReservationResponse(
            reservation.getId(),
            reservation.getDate().getDate(), // TODO: 네이밍 변경!!!
            reservation.getTime().getStartAt(),
            ThemePayload.from(reservation.getTheme())
        );
    }

    public record ThemePayload(
        String name,
        String content,
        String url
    ) {

        public static ThemePayload from(Theme theme) {
            return new ThemePayload(
                theme.getName(),
                theme.getContent(),
                theme.getUrl()
            );
        }
    }
}
