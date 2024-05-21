package roomescape.dto.reservation;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;

public record UserReservationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long timeId,
        Long themeId
) {

    public UserReservationRequest {
        Objects.requireNonNull(date);
        Objects.requireNonNull(timeId);
        Objects.requireNonNull(themeId);
    }
}
