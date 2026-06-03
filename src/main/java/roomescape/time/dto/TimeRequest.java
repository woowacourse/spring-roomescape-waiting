package roomescape.time.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record TimeRequest(
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt) {
}
