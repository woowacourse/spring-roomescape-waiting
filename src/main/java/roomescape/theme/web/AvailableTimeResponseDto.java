package roomescape.theme.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.time.web.TimeResponseDto;

public record AvailableTimeResponseDto(
        @JsonProperty("time") TimeResponseDto timeResponseDto,
        boolean alreadyBooked
) {
}
