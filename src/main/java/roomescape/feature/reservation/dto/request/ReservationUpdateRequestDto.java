package roomescape.feature.reservation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReservationUpdateRequestDto(
    @NotBlank(message = "예약자명은 필수입니다.")
    @Size(max = 20, message = "예약자명의 길이는 1이상 20이하 입니다.")
    String name,

    @FutureOrPresent(message = "예약 날짜가 현재보다 과거입니다.")
    LocalDate date,

    @Positive(message = "timeId의 값이 양수가 아닙니다.")
    Long timeId,

    @Positive(message = "themeId의 값이 양수가 아닙니다.")
    Long themeId) {

}
