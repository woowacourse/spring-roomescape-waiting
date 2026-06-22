package roomescape.payment.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.payment.application.dto.PaymentOrderCommand;

public record PaymentOrderRequest(
        @NotBlank String name,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId
) {
    public PaymentOrderCommand toCommand() {
        return new PaymentOrderCommand(name, date, themeId, timeId);
    }
}
