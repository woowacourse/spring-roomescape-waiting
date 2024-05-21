package roomescape.service.dto.input;

import java.time.LocalDate;

public record AvailableReservationTimeInput(long themeId, LocalDate date) {

}
