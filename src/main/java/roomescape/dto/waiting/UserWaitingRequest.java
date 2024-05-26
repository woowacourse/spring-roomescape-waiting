package roomescape.dto.waiting;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;

public record UserWaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long timeId,
        Long themeId
) {

    public UserWaitingRequest {
        Objects.requireNonNull(date);
        Objects.requireNonNull(timeId);
        Objects.requireNonNull(themeId);
    }
}
