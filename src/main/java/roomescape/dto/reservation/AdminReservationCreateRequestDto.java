package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record AdminReservationCreateRequestDto(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        long themeId,
        long timeId,
        long memberId) {
}
