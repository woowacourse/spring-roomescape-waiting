package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AvailableTimeResponseDto(
        @JsonProperty("time") TimeResponseDto timeResponseDto,
        boolean alreadyBooked
) {
}
