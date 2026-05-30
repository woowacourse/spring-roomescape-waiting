package roomescape.time.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeRequest(
        @NotNull(message = "시간은 필수로 입력해야 합니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt) {
}
