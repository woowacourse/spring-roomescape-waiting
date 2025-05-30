package roomescape.reservation.application.dto.request;

import java.time.LocalDate;

public record WaitingReservationCreateRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
}
