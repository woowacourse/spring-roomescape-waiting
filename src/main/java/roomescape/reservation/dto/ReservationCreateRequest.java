package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record ReservationCreateRequest(Long memberId,
                                       @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                       Long timeId,
                                       Long themeId) {
}
