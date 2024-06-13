package roomescape.reservation.dto;

import java.time.LocalDate;

public record SaveWaitingRequest(LocalDate date, Long themeId, Long timeId) {
}
