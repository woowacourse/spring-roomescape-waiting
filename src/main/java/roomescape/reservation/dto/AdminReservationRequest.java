package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationRequest(
        @NotNull(message = "날짜가 존재하지 않습니다.") LocalDate date,
        long memberId,
        long timeId,
        long themeId
) {

}
