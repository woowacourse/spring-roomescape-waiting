package roomescape.controller.client.api.dto.condition;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestParam;

public record ReservationSearchCondition(
        @NotBlank(message = "예약 정보 검색 시 사용자 명이 필요합니다.")
        @RequestParam String name
) {
}
