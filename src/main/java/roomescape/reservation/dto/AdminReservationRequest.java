package roomescape.reservation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record AdminReservationRequest(
        @NotNull(message = "날짜가 선택되지 않았습니다.")
        LocalDate date,
        @NotNull(message = "예약자 정보가 없습니다.")
        Long memberId,
        @NotNull(message = "시간이 선택되지 않았습니다.")
        Long timeId,
        @NotNull(message = "테마가 선택되지 않았습니다.")
        Long themeId
) {

}
