package roomescape.reservationtime.dto;

import java.util.List;
import java.util.Map;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeAvailability;

public record AvailableTimeResponse(
        Long id,
        String time,
        Boolean available,
        Boolean waitable,
        Long reservationId
) {
    public static AvailableTimeResponse from(ReservationTime reservationTime, Long reservationId) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().toString(),
                reservationId == null,
                reservationId != null,
                reservationId
        );
    }

    public static AvailableTimeResponse from(ReservationTimeAvailability availability) {
        return new AvailableTimeResponse(
                availability.reservationTime().getId(),
                availability.reservationTime().getStartAt().toString(),
                availability.isAvailable(),
                availability.isWaitable(),
                availability.isWaitable() ? availability.reservationId() : null
        );
    }

    public static List<AvailableTimeResponse> fromAll(Map<ReservationTime, Long> reservationTimesAvailability) {
        return reservationTimesAvailability.entrySet().stream()
                .map(entry -> from(entry.getKey(), entry.getValue()))
                .toList();
    }
}
