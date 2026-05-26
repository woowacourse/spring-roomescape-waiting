package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.service.dto.WaitingCommand;

public record WaitingRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId
) {

    public WaitingCommand toCommand() {
        return new WaitingCommand(
                this.name(),
                this.date(),
                this.timeId(),
                this.themeId());
    }
}
