package roomescape.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import roomescape.service.command.ReservationUpdateCommand;

import java.time.LocalDate;

public record ReservationUpdateRequest(
        @NotNull(message = "날짜는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        @Min(value = 1, message = "유효한 시간 ID가 필요합니다.")
        long timeId
) {
    public static ReservationUpdateCommand toCommand(ReservationUpdateRequest request) {
        return new ReservationUpdateCommand(
                request.date,
                request.timeId
        );
    }
}
