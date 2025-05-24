package roomescape.reservation.ui.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
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
            String themeName,
            LocalDate date,
            @JsonFormat(pattern = "HH:mm")
            LocalTime time,
            String status
    ) {

        public static ForMember from(final Reservation reservation) {
            return new ForMember(
                    reservation.getId(),
                    reservation.getReservationSlot().getTheme().getName(),
                    reservation.getReservationSlot().getDate(),
                    reservation.getReservationSlot().getTime().getStartAt(),
                    reservation.getStatus().getDescription()
            );
        }
    }
}
