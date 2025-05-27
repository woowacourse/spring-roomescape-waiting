package roomescape.reservation.dto;

import java.time.LocalDate;

public record CreateWaitingRequest(
        LocalDate date,
        Long theme,
        Long time
) {
}
