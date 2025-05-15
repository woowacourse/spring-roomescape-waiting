package roomescape.reservationTime.dto.request;

import java.time.LocalDate;

public record TimeConditionRequest(LocalDate date, Long themeId) {
}
