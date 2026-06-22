package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;

public record ReservationRequest(
        @NotBlank(message = "예약자 이름을 입력해 주세요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z]{2,30}$",
                message = "닉네임은 2자 이상 30자 이하의 한글 또는 영문만 사용할 수 있습니다."
        )
        String name,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "날짜를 입력해 주세요.")
        LocalDate date,

        @NotNull(message = "시간을 선택해 주세요.")
        Long timeId,

        @NotNull(message = "테마를 선택해 주세요.")
        Long themeId,

        @NotNull(message = "결제 금액을 입력해 주세요.")
        Long amount
) {
    public Reservation toReservation(Slot slot, LocalDateTime dateTime) {
        return Reservation.createFutureReservation(name, slot, dateTime);
    }
}
