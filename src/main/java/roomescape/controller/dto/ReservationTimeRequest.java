package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ReservationTimeRequest(

        @NotNull(message = "startAt은 비어 있을 수 없습니다.")
        LocalTime startAt) {
}
