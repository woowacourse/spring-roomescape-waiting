package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationUpdateRequest(

        @NotNull(message = "변경할 날짜를 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "변경할 시간 ID를 입력해주세요.")
        @Positive(message = "시간 ID는 1 이상이어야 합니다.")
        Long timeId
) {

}
