package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.presentation.dto.ReservationTimeResponse;
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
                        reservation.getMember().getName())
        );
    }
}
