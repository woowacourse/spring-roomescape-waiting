package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record MemberReservationRequest(
        @NotNull(message = "예약 시간 id는 필수 값입니다.")
        @Positive
        Long memberId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        @NotNull(message = "예약 날짜는 필수 값입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간 id는 필수 값입니다.")
        @Positive
        Long timeId,

        @NotNull(message = "테마 id는 필수 값입니다.")
        @Positive
        Long themeId
) {
}
