package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record WaitingReservationResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member
) {
    public WaitingReservationResponse(final WaitingReservation waitingReservation) {
        this(
                waitingReservation.getId(),
                waitingReservation.getRoomEscapeInformation().getDate(),
                new ReservationTimeResponse(waitingReservation.getRoomEscapeInformation().getTime()),
                new ThemeResponse(waitingReservation.getRoomEscapeInformation().getTheme()),
                new MemberResponse(waitingReservation.getMember())
        );
    }
}
