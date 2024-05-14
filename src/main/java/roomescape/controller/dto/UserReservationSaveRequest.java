package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.service.dto.ReservationSaveRequest;

public record UserReservationSaveRequest(
        @NotNull
        @Future(message = "지나간 날짜의 예약을 할 수 없습니다.")
        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        @NotNull
        @Positive
        Long timeId,

        @NotNull
        @Positive
        Long themeId
) {
        public ReservationSaveRequest toReservationSaveRequest(Long memberId) {
                return new ReservationSaveRequest(memberId, date, timeId, themeId);
        }
}
