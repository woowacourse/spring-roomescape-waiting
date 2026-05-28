package roomescape.domain;

import java.time.LocalDate;

public record WaitingReservation(
        long id,
        String name,
        LocalDate date,
        Time time,
        Theme theme,
        String status,
        int waitingOrder
) {
}
