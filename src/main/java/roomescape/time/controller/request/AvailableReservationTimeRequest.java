package roomescape.time.controller.request;

import java.time.LocalDate;

public record AvailableReservationTimeRequest(LocalDate date, Long themeId) {
}
