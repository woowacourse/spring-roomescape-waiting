package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.TimeSlot;

public record TimeSlotRequest(
        @NotNull(message = "시작 시간은 비어있을 수 없습니다.") @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public TimeSlot toEntity() {
        return new TimeSlot(null, startAt);
    }
}
