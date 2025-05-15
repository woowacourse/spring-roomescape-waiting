package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;

public record TimeDataWithBookingInfoResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static List<TimeDataWithBookingInfoResponse> from(List<TimeDataWithBookingInfo> dtos) {
        return dtos.stream()
                .map(dto -> new TimeDataWithBookingInfoResponse(
                                dto.id(),
                                dto.startAt(),
                                dto.alreadyBooked()
                        )
                )
                .toList();
    }
}
