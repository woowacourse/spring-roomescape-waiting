package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.TimeSlot;

public record TimeSlotRequest(@JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public TimeSlotRequest {
        isValid(startAt);
    }

    public TimeSlot toEntity() {
        return new TimeSlot(null, startAt);
    }

    private void isValid(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("잘못된 시간입니다");
        }
    }
}
