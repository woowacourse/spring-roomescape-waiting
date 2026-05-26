package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record UserReservationResponse(
    String name,
    List<ReservationPayload> reservation
) {

    public static UserReservationResponse of(String name, List<Reservation> reservation) {
        return new UserReservationResponse(
            name,
            reservation.stream()
                .map(ReservationPayload::from)
                .toList()
        );
    }

    public record ReservationPayload(
        Long reservationId,
        ReservationDatePayload date,
        ReservationTimePayload time,
        ThemePayload theme
    ) {

        public static ReservationPayload from(Reservation reservation) {
            return new ReservationPayload(
                reservation.getId(),
                ReservationDatePayload.from(reservation.getDate()),
                ReservationTimePayload.from(reservation.getTime()),
                ThemePayload.from(reservation.getTheme())
            );
        }
    }

    public record ReservationDatePayload(
        Long id,
        LocalDate startWhen
    ) {

        public static ReservationDatePayload from(ReservationDate date) {
            return new ReservationDatePayload(date.getId(), date.getDate());
        }
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
