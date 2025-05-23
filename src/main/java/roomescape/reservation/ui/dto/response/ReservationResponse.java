package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.ui.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        IdName member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                IdName.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().getDescription()
        );
    }

    public record ForMember(
            Long id,
            String themeName,
            LocalDate date,
            LocalTime time,
            String status
    ) {

        public static ForMember from(final Reservation reservation) {
            return new ForMember(
                    reservation.getId(),
                    reservation.getTheme().getName(),
                    reservation.getDate(),
                    reservation.getTime().getStartAt(),
                    reservation.getStatus().getDescription()
            );
        }
    }
}
