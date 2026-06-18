package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationTimePayload time,
    ThemePayload theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getMember().getName(),
            reservation.getDate().getPlayDay(),
            ReservationTimePayload.from(reservation.getTime()),
            ThemePayload.from(reservation.getTheme())
        );
    }

    public record ReservationTimePayload(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
    ) {

        public static ReservationTimePayload from(ReservationTime reservationTime) {
            return new ReservationTimePayload(reservationTime.getId(), reservationTime.getStartAt());
        }
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
                theme.getContent(),
                theme.getUrl()
            );
        }
    }
}
