package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.theme.domain.Theme;

public record ReservationResponse(
        Long id,
        Member member,
        Theme theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time
) {
}
