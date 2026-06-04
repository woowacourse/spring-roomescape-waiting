package roomescape.reservationtime.dto;

import java.util.List;
import java.util.Map;
import roomescape.reservationtime.ReservationTime;

public record AvailableTimeResponse(
        Long id,
        String time,
        Boolean available,
        Long reservationId
) {
    public static AvailableTimeResponse from(ReservationTime reservationTime, Long reservationId) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().toString(),
                reservationId == null,
                reservationId
        );
    }

    public static List<AvailableTimeResponse> fromAll(Map<ReservationTime, Long> reservationTimesAvailability) {
        return reservationTimesAvailability.entrySet().stream()
                .map(entry -> from(entry.getKey(), entry.getValue()))
                .toList();
    }
}
