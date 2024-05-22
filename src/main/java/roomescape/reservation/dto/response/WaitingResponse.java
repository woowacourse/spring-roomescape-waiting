package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ThemeResponse theme,
        TimeResponse time
) {

    public static WaitingResponse toResponse(Reservation reservation) {
        return new WaitingResponse(
                reservation.getId(),
                MemberResponse.toResponse(reservation.getMember()),
                reservation.getDate(),
                ThemeResponse.toResponse(reservation.getTheme()),
                TimeResponse.toResponse(reservation.getTime())
        );
    }
}
