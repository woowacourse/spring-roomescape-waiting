package roomescape.reservation.presentation.dto;

import java.time.LocalDate;

public record AdminReservationRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
}
