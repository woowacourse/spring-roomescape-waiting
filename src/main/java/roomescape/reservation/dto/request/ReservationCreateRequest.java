package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import roomescape.global.exception.InvalidInputException;

public record ReservationCreateRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") @NotNull(message = "예약할 날짜가 입력되지 않았다.") LocalDate date,
    @NotNull(message = "예약할 시간이 입력되지 않았다.") Long timeId,
    @NotNull(message = "예약할 테마가 입력되지 않았다.") Long themeId) {

}
