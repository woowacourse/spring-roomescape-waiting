package roomescape.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MemberReservation(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        LocalDateTime createAt,
        Long order) {
}


