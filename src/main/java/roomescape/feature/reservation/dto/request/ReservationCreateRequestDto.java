package roomescape.feature.reservation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationCreateRequestDto(
    String name,

    @NotNull(message = "예약 날짜는 필수입니다.")
    @FutureOrPresent(message = "예약 날짜가 현재보다 과거입니다.")
    LocalDate date,

    @NotNull(message = "timeId는 필수입니다.")
    @Positive(message = "timeId의 값이 유효하지 않습니다.")
    Long timeId,

    @NotNull(message = "themeId는 필수입니다.")
    @Positive(message = "themeId의 값이 유효하지 않습니다.")
    Long themeId) {

}
