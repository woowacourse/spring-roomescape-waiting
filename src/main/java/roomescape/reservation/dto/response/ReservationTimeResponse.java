package roomescape.reservation.dto.response;

import roomescape.reservation.entity.ReservationTime;

public class ReservationTimeResponse {

    public record ReservationTimeCreateResponse(
            Long id,
            String startAt
    ) {
        public static ReservationTimeCreateResponse from(ReservationTime reservationTime) {
            return new ReservationTimeCreateResponse(reservationTime.getId(), reservationTime.getFormattedTime());
        }
    }

    public record ReservationTimeReadResponse(
            Long id,
            String startAt
    ) {
        public static ReservationTimeReadResponse from(ReservationTime reservationTime) {
            return new ReservationTimeReadResponse(reservationTime.getId(), reservationTime.getFormattedTime());
        }
    }

    public record AvailableReservationTimeResponse(
            Long id,
            String startAt,
            boolean alreadyBooked
    ) {
    }
}