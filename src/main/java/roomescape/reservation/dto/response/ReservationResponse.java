package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.response.ReservationMemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;

public record ReservationResponse(Long id, String status, ReservationMemberResponse member, LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme) {
    public static ReservationResponse from(final Reservation reservation) {
        String status = "예약";
        if(reservation.isWaitingStatus()){
            status = "대기";
        }
        return new ReservationResponse(
                reservation.getId(),
                status,
                new ReservationMemberResponse(reservation.name()),
                reservation.getDate(),
                new ReservationTimeResponse(
                        reservation.timeId(),
                        reservation.reservationTime()
                ),
                new ThemeResponse(reservation.themeId(),
                        reservation.themeName(),
                        reservation.themeDescription(),
                        reservation.themeThumbnail())
        );
    }
}
