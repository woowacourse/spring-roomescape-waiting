package roomescape.presentation.reservation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;

public record ReservationCreateResponse(
        Long id,

        LocalDate date,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,

        ThemePayload theme
) {

    public static ReservationCreateResponse from(Reservation reservation) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getSlot().getDate(),
                reservation.getSlot().getTime().getStartAt(),
                ThemePayload.from(reservation.getSlot().getTheme())
        );
    }

    public record ThemePayload(
            Long id,
            String name,
            String content,
            String url
    ) {

        public static ThemePayload from(Theme theme) {
            return new ThemePayload(
                    theme.getId(),
                    theme.getName(),
                    theme.getDescription(),
                    theme.getThumbnailUrl()
            );
        }
    }
}
