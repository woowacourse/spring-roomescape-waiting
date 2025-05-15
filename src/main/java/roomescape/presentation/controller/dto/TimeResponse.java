package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.application.dto.TimeDto;

public record TimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static TimeResponse from(TimeDto timeDto) {
        return new TimeResponse(
                timeDto.id(),
                timeDto.startAt()
        );
    }

    public static List<TimeResponse> from(List<TimeDto> timeDtos) {
        return timeDtos.stream()
                .map(timeDto -> new TimeResponse(
                                timeDto.id(),
                                timeDto.startAt()
                        )
                )
                .toList();
    }
}
