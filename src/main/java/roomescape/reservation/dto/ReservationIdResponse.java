package roomescape.reservation.dto;

public record ReservationIdResponse(Long id) {
    public static ReservationIdResponse from(Long id) {
        return new ReservationIdResponse(id);
    }
}
