package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.ui.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        IdName member,
        String status
) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationSlot().getDate(),
                ReservationTimeResponse.from(reservation.getReservationSlot().getTime()),
                ThemeResponse.from(reservation.getReservationSlot().getTheme()),
                IdName.from(reservation.getMember()),
                reservation.getStatus().getDescription()
        );
    }

    public record ForMember(
            Long id,
            LocalDate date,
            ReservationTimeResponse time,
            ThemeResponse theme,
            String status
    ) {

        public static ForMember from(final Reservation reservation) {
            return new ForMember(
                    reservation.getId(),
                    reservation.getReservationSlot().getDate(),
                    ReservationTimeResponse.from(reservation.getReservationSlot().getTime()),
                    ThemeResponse.from(reservation.getReservationSlot().getTheme()),
                    reservation.getStatus().getDescription()
            );
        }
    }
}
