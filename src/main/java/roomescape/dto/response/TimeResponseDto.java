package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.Time;

public record TimeResponseDto(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
    public static TimeResponseDto from(Time time) {
        return new TimeResponseDto(time.getId(), time.getStartAt());
    }
}
