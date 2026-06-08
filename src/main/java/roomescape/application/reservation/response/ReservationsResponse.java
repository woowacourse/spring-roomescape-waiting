package roomescape.application.reservation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
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
            ReservationSlotPayload slot,
            String username,
            Integer waitingNumber,
            ReservationStatus status
    ) {
        private static ReservationPayload from(Reservation reservation) {
            return new ReservationPayload(
                    reservation.getId(),
                    ReservationSlotPayload.from(reservation.getSlot()),
                    reservation.getUser().getName(),
                    reservation.getWaitingNumber(),
                    reservation.getStatus()
            );
        }
    }

    private record ReservationSlotPayload(
            Long id,
            LocalDate date,
            ReservationTimePayload startAt,
            ThemePayload theme
    ) {
        private static ReservationSlotPayload from(ReservationSlot slot) {
            return new ReservationSlotPayload(
                    slot.getId(),
                    slot.getDate(),
                    ReservationTimePayload.from(slot.getTime()),
                    ThemePayload.from(slot.getTheme())
            );
        }
    }

    private record ReservationTimePayload(
            Long id,
            @JsonFormat(pattern = "HH:mm")
            LocalTime startAt
    ) {
        private static ReservationTimePayload from(ReservationTime time) {
            return new ReservationTimePayload(time.getId(), time.getStartAt());
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
