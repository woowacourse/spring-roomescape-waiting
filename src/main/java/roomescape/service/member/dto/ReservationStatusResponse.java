package roomescape.service.member.dto;

import roomescape.domain.reservation.Reservation;

public record ReservationStatusResponse(String status, long rank) {
}
