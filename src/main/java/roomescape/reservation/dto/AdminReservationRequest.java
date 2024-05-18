package roomescape.reservation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record AdminReservationRequest(
        @NotNull(message = "날짜가 존재하지 않습니다.") LocalDate date,
        long memberId,
        long timeId,
        long themeId
) {

}
