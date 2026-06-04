package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.reservation.application.dto.ReservationCancelCommand;

public record ReservationCancelRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String username
) {
    public ReservationCancelCommand toCommand() {
        return ReservationCancelCommand.builder()
                .name(username)
                .build();
    }
}
