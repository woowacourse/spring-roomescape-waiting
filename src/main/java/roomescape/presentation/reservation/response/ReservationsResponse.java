package roomescape.presentation.reservation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;

public record ReservationsResponse(
        List<ReservationPayload> reservations
) {

    public static ReservationsResponse of(List<Reservation> reservations) {
        List<ReservationPayload> payloads = reservations.stream()
                .map(ReservationPayload::from)
                .toList();

        return new ReservationsResponse(payloads);
    }

    private record ReservationPayload(
            Long id,
            String username,
            ReservationSlotPayload slot,
            Integer waitingNumber,
            ReservationStatus status
    ) {
        private static ReservationPayload from(Reservation reservation) {
            return new ReservationPayload(
                    reservation.getId(),
                    reservation.getUser().getName(),
                    ReservationSlotPayload.from(reservation.getSlot()),
                    reservation.getWaitingNumber(),
                    reservation.getStatus()
            );
        }
    }

    private record ReservationSlotPayload(
            Long id,
            ThemePayload theme,
            LocalDate date,
            @JsonFormat(pattern = "HH:mm")
            LocalTime startAt
    ) {

        private static ReservationSlotPayload from(ReservationSlot slot) {
            return new ReservationSlotPayload(
                    slot.getId(),
                    ThemePayload.from(slot.getTheme()),
                    slot.getDate(),
                    slot.getTime().getStartAt()
            );
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
                    theme.getDescription(),
                    theme.getThumbnailUrl()
            );
        }
    }
}
