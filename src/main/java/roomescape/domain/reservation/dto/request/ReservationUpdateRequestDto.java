package roomescape.domain.reservation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record ReservationUpdateRequestDto(
    @FutureOrPresent(message = "예약 날짜가 현재보다 과거입니다.")
    LocalDate date,

    @Positive(message = "timeId의 값이 양수가 아닙니다.")
    Long timeId,

    @Positive(message = "themeId의 값이 양수가 아닙니다.")
    Long themeId,

    @NotNull(message = "version은 필수입니다.")
    @PositiveOrZero(message = "version의 값이 음수입니다.")
    Long version) {

}
