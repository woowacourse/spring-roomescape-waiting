package roomescape.theme.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.time.web.dto.TimeResponseDto;

public record AvailableTimeResponseDto(
        @JsonProperty("time") TimeResponseDto timeResponseDto,
        boolean alreadyBooked
) {
}
