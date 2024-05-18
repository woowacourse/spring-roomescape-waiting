package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationSaveRequest(
        @NotNull
        @Positive
        Long memberId,

        @NotNull
        @Future(message = "지나간 날짜의 예약을 할 수 없습니다.")
        LocalDate date,

        @NotNull
        @Positive
        Long timeId,

        @NotNull
        @Positive
        Long themeId
) {
}
