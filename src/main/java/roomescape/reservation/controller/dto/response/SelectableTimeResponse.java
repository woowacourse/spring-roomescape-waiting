package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.SelectableTime;

public record SelectableTimeResponse(
        long id,

        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt,

        boolean alreadyBooked
) {

    public static SelectableTimeResponse from(final SelectableTime selectableTime) {
        return new SelectableTimeResponse(
                selectableTime.timeId(),
                selectableTime.startAt(),
                selectableTime.alreadyBooked()
        );
    }

    public static List<SelectableTimeResponse> list(final List<SelectableTime> selectableTimes) {
        return selectableTimes.stream()
                .map(SelectableTimeResponse::from)
                .toList();
    }
}
