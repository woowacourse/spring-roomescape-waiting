package roomescape.time.controller.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import roomescape.time.domain.ReservationTime;

public record ReservationTimeResponse(Long id, String startAt) {

    public static List<ReservationTimeResponse> from(List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().format(formatter)
        );
    }
}
