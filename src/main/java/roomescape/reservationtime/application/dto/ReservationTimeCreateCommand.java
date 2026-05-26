package roomescape.reservationtime.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeCreateCommand(
        @NotNull(message = "[ERROR] 시간은 비어있을 수 없습니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public ReservationTime toEntity() {
        return ReservationTime.builder()
                .startAt(startAt)
                .build();
    }
}
