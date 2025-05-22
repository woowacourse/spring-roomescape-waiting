package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationAdminRequest(
        @NotNull(message = "날짜는 필수값입니다.")
        LocalDate date,

        @NotNull(message = "테마는 필수값입니다.")
        Long themeId,

        @NotNull(message = "예약 시간은 필수값입니다.")
        Long timeId,

        @NotNull(message = "회원은 필수값입니다.")
        Long memberId) {
}
