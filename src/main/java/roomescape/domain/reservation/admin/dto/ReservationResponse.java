package roomescape.domain.reservation.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationResponse(
    Long id,
    LocalDate date,
    ReservationTimePayload time,
    ThemePayload theme,
    String userName,
    Long waitingNumber,
    ReservationStatus reservationStatus
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getReservationSlot().getDate().getDate(),
            ReservationTimePayload.from(reservation.getReservationSlot().getTime()),
            ThemePayload.from(reservation.getReservationSlot().getTheme()),
            reservation.getUser().getName(),
            reservation.getWaitingNumber(),
            reservation.getStatus()
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
