package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationEditRequest(
        @NotNull(message = "예약 날짜는 비어 있을 수 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "예약 시간 id는 비어 있을 수 없습니다.")
        Long timeId
) {
}
