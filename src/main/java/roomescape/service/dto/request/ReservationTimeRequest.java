package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull(message = "날짜를 입력해주세요.")
        LocalTime startAt) {

    public ReservationTime toEntity() {
        return new ReservationTime(startAt);
    }
}
