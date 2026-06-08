package roomescape.controller.client.dto.condition;

import jakarta.validation.constraints.NotBlank;
import roomescape.service.command.ReservationSearchCommand;

public record ReservationSearchCondition(
        @NotBlank(message = "예약 정보 검색 시 사용자 명이 필요합니다.")
        String name
) {

    public ReservationSearchCommand toCommand() {
        return new ReservationSearchCommand(name);
    }
}
