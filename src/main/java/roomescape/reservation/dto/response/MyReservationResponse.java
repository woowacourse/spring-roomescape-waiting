package roomescape.reservation.dto.response;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String ReservedStatus) {

    public static MyReservationResponse from(final MyReservationOutput reservation) {
        return new MyReservationResponse(reservation.reservationId(), reservation.theme(),
                reservation.date(), reservation.time(), "예약");
    }
}
