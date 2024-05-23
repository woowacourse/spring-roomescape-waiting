package roomescape.domain.time.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public record BookableTimeResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("startAt")
    public LocalTime getFormattedStartAt() {
        return startAt;
    }
}
