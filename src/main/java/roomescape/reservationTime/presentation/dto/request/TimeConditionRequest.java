package roomescape.reservationTime.presentation.dto.request;

import java.time.LocalDate;

public record TimeConditionRequest(LocalDate date, Long themeId) {
}
