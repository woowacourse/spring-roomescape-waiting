package roomescape.controller.client.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.application.facade.command.ReservationOrderCommand;
import roomescape.domain.order.OrderType;

public record OrderRequest(
        @NotBlank(message = "예약자 이름은 필수 값입니다.")
        String name,

        @NotNull(message = "예약 날짜는 필수 값입니다.")
        @FutureOrPresent(message = "예약 날짜는 과거일 수 없습니다.")
        LocalDate date,

        @NotNull(message = "테마는 필수 값입니다.")
        @Positive(message = "테마 식별자는 양수입니다.")
        Long themeId,

        @NotNull(message = "시간은 필수 값입니다.")
        @Positive(message = "시간 식별자는 양수입니다.")
        Long timeId,

        @NotNull(message = "주문 타입은 필수 값입니다.")
        OrderType orderType
) {
    public ReservationOrderCommand toCommand() {
        return new ReservationOrderCommand(name, date, themeId, timeId, orderType);
    }
}
