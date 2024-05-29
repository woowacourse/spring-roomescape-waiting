package roomescape.reservation.dto;

import java.time.LocalDate;

public record AdminReservationRequest(LocalDate date, Long themeId, Long timeId, Long memberId) {
}
