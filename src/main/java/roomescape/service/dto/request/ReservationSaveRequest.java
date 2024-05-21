package roomescape.service.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReservationSaveRequest(
        @NotNull(message = "멤버 아이디를 입력해주세요")
        @Positive(message = " 멤버 아이디는 양수여야 해요")
        Long memberId,

        @NotNull(message = "예약 날짜를 입력해주세요")
        @Future(message = "지나간 날짜의 예약을 할 수 없습니다.")
        LocalDate date,

        @NotNull(message = "예약 시간을 선택해주세요")
        @Positive(message = "잘못된 예약 시간 요청입니다")
        Long timeId,

        @NotNull(message = "테마를 선택해주세요")
        @Positive(message = "잘못된 테마 선택 요청입니다.")
        Long themeId
) {
}
