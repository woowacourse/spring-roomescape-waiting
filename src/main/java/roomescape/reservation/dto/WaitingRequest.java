package roomescape.reservation.dto;

import java.time.LocalDate;

public record WaitingRequest(LocalDate date, Long theme, Long time) {
}
