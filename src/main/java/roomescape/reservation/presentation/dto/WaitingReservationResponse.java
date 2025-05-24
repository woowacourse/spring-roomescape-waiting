package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;
import roomescape.theme.presentation.dto.ThemeResponse;

public record WaitingReservationResponse(Long id, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, MemberResponse member) {
    public static WaitingReservationResponse from(final WaitingReservation waitingReservation) {
        return new WaitingReservationResponse(
            waitingReservation.getId(),
            waitingReservation.getDate(),
            new ReservationTimeResponse(
                waitingReservation.getTime().getId(),
                waitingReservation.getTime().getStartAt()
            ),
            new ThemeResponse(waitingReservation.getTheme().getId(),
                waitingReservation.getTheme().getName(),
                waitingReservation.getTheme().getDescription(),
                waitingReservation.getTheme().getThumbnail()
            ),
            new MemberResponse(waitingReservation.getMember().getId(),
                waitingReservation.getMember().getName().name())
        );
    }
}
