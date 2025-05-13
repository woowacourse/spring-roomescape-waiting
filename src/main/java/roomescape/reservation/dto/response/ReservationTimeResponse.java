package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.entity.ReservationTime;

public class ReservationTimeResponse {

    public record ReservationTimeCreateResponse(
            Long id,
            @JsonFormat(pattern = "HH:mm") LocalTime startAt
    ) {
        public static ReservationTimeCreateResponse from(ReservationTime reservationTime) {
            return new ReservationTimeCreateResponse(reservationTime.getId(), reservationTime.getStartAt());
        }
    }

    public record ReservationTimeReadResponse(
            Long id,
            @JsonFormat(pattern = "HH:mm") LocalTime startAt
    ) {
        public static ReservationTimeReadResponse from(ReservationTime reservationTime) {
            return new ReservationTimeReadResponse(reservationTime.getId(), reservationTime.getStartAt());
        }
    }

    public record AvailableReservationTimeResponse(
            Long id,
            @JsonFormat(pattern = "HH:mm") LocalTime startAt,
            boolean alreadyBooked
    ) {
    }
}