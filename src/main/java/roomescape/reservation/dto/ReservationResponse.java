package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        ThemeResponse theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time
) {
    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime())
        );
    }
}
