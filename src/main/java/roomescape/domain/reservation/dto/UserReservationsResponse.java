package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record UserReservationsResponse(
    String username,
    List<ReservationPayload> reservations
) {

    public static UserReservationsResponse of(String username, List<ReservationWithWaitingNumber> reservations) {
        return new UserReservationsResponse(
            username,
            reservations.stream()
                .map(ReservationPayload::from)
                .toList()
        );
    }

    private record ReservationPayload(
        Long id,
        ReservationSlotPayload reservationSlot,
        String status,
        Long waitingNumber
    ) {

        private static ReservationPayload from(ReservationWithWaitingNumber reservation) {
            return new ReservationPayload(
                reservation.reservation().getId(),
                ReservationSlotPayload.from(reservation.reservation().getReservationSlot()),
                reservation.reservation().getStatus().toString(),
                reservation.waitingNumber()
            );
        }
    }

    private record ReservationSlotPayload(
        Long id,
        ReservationDatePayload date,
        ReservationTimePayload time,
        ThemePayload theme
    ) {

        private static ReservationSlotPayload from(ReservationSlot reservationSlot) {
            return new ReservationSlotPayload(
                reservationSlot.getId(),
                ReservationDatePayload.from(reservationSlot.getDate()),
                ReservationTimePayload.from(reservationSlot.getTime()),
                ThemePayload.from(reservationSlot.getTheme())
            );
        }
    }

    private record ReservationDatePayload(
        Long id,
        LocalDate startWhen
    ) {

        private static ReservationDatePayload from(ReservationDate date) {
            return new ReservationDatePayload(date.getId(), date.getDate());
        }
    }


    private record ReservationTimePayload(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
    ) {

        private static ReservationTimePayload from(ReservationTime reservationTime) {
            return new ReservationTimePayload(reservationTime.getId(), reservationTime.getStartAt());
        }
    }

    private record ThemePayload(
        Long id,
        String name,
        String content,
        String url
    ) {

        private static ThemePayload from(Theme theme) {
            return new ThemePayload(
                theme.getId(),
                theme.getName(),
                theme.getContent(),
                theme.getUrl()
            );
        }
    }
}
