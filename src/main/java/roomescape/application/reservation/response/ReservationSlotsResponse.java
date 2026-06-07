package roomescape.application.reservation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.ReservationCountResult;

public record ReservationSlotsResponse(
        List<ReservationSlotPayload> reservationSlots
) {

    public static ReservationSlotsResponse from(List<ReservationCountResult> results) {
        List<ReservationSlotPayload> payloads = results.stream()
                .map(ReservationSlotPayload::from)
                .toList();

        return new ReservationSlotsResponse(payloads);
    }

    private record ReservationSlotPayload(
            Long slotId,
            Long timeId,
            @JsonFormat(pattern = "HH:mm")
            LocalTime startAt,
            Long waitingNumber
    ) {
        private static ReservationSlotPayload from(ReservationCountResult result) {
            return new ReservationSlotPayload(
                    result.slotId(),
                    result.timeId(),
                    result.startAt(),
                    result.waitingCount()
            );
        }
    }
}
