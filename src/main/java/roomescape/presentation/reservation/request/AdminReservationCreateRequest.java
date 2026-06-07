package roomescape.presentation.reservation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationCreateRequest(
        @NotBlank(message = "이름은 비어있을 수 없습니다.")
        String username,

        @NotNull(message = "슬롯은 필수 선택 사항 입니다. 슬롯을 선택해주세요.")
        Long slotId
) {
}
