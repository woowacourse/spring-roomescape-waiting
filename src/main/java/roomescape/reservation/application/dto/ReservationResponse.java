package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.application.dto.TimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        ThemeResponse theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        TimeResponse time
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), MemberResponse.from(reservation.getMember()),
                ThemeResponse.from(reservation.getTheme()), reservation.getDate(),
                TimeResponse.from(reservation.getTime()));
    }
}
