package roomescape.reservation.application.dto;

public record ReservationSearchCondition(String username) {
    public boolean hasUsername() { return username != null; }
}
