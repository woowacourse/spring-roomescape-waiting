package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.theme.presentation.dto.ThemeResponse;

public record ReservationResponse(Long id, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, MemberResponse member) {
    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                new ReservationTimeResponse(
                        reservation.getTime().getId(),
                        reservation.getTime().getStartAt()
                ),
                new ThemeResponse(reservation.getTheme().getId(),
                        reservation.getTheme().getName(),
                        reservation.getTheme().getDescription(),
                        reservation.getTheme().getThumbnail()
                ),
                new MemberResponse(reservation.getMember().getId(),
                        reservation.getMember().getName().name())
        );
    }

    public static ReservationResponse from(final WaitingReservation waitingReservation) {
        return new ReservationResponse(
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
