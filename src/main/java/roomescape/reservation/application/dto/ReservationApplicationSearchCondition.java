package roomescape.reservation.application.dto;

public record ReservationApplicationSearchCondition(String username) {
    public boolean hasUsername() { return username != null; }
}
