package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record MemberReservationCreateRequestDto
        (@JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
         Long themeId,
         Long timeId
) {
}
