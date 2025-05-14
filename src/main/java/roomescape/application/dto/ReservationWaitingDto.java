package roomescape.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWaitingDto(
        long reservationid,
        String theme,
        LocalDate date,
        // TODO: 시간 포맷 지정
        LocalTime time,
        String status
) {
}
