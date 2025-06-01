package roomescape.reservation.application.dto.request;

import java.time.LocalDate;

public record ConfirmedReservationByCriteriaWebRequest(Long themeId, Long memberId, LocalDate startDate,
                                                       LocalDate endDate) {
}
