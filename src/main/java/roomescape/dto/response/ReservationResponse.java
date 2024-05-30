package roomescape.dto.response;

import roomescape.domain.Reservation;

import java.time.LocalDate;

import static roomescape.domain.Reservation.Status;

public record ReservationResponse(
        Long id,
        MemberPreviewResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Status status) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberPreviewResponse.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus()
        );
    }
}
