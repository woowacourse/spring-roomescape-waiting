package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReservationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "날짜를 입력해 주세요.")
        LocalDate date,

        @NotNull(message = "시간을 선택해 주세요.")
        Long timeId
) {
}
