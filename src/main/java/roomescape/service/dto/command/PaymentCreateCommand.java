package roomescape.service.dto.command;

import jakarta.validation.constraints.NotNull;

public record PaymentCreateCommand(
        @NotNull(message = "예약 ID는 비워둘 수 없습니다.")
        Long reservationId,

        @NotNull(message = "주문 금액은 비워둘 수 없습니다.")
        Long price
) {
}
