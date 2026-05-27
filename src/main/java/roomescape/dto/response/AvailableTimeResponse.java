package roomescape.dto.response;

import roomescape.domain.ReservationTime;

import java.util.List;
import java.util.Map;

public record AvailableTimeResponse(
        Long id,
        String time,
        Boolean available
) {
    public static AvailableTimeResponse from(ReservationTime reservationTime, Boolean available) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().toString(),
                available
        );
    }

    public static List<AvailableTimeResponse> fromAll(Map<ReservationTime, Boolean> reservationTimesAvailability) {
        return reservationTimesAvailability.entrySet().stream()
                .map(entry -> from(entry.getKey(), entry.getValue()))
                .toList();
    }
}
