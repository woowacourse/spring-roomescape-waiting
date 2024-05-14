package roomescape.web.dto.request.reservation;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserReservationRequest
        (@Future(message = "과거에 대한 예약은 할 수 없습니다.") @NotNull(message = "날짜에 빈값은 허용하지 않습니다.") LocalDate date,
         @Min(value = 1, message = "타임 아이디는 1이상의 정수만 허용합니다.") Long timeId,
         @Min(value = 1, message = "테마 아이디는 1이상의 정수만 허용합니다.") Long themeId) {

}
