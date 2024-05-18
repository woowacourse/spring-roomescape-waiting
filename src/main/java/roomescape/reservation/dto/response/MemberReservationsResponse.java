package roomescape.reservation.dto.response;

import java.util.List;

public record MemberReservationsResponse(List<MemberReservationResponse> reservations) {
}
