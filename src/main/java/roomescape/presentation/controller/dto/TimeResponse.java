package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.application.dto.TimeServiceResponse;

public record TimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static TimeResponse from(TimeServiceResponse timeServiceResponse) {
        return new TimeResponse(
                timeServiceResponse.id(),
                timeServiceResponse.startAt()
        );
    }

    public static List<TimeResponse> from(List<TimeServiceResponse> timeServiceResponses) {
        return timeServiceResponses.stream()
                .map(timeDto -> new TimeResponse(
                                timeDto.id(),
                                timeDto.startAt()
                        )
                )
                .toList();
    }
}
