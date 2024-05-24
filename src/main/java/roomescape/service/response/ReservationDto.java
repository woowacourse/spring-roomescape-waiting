package roomescape.service.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;

public record ReservationDto(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeDto reservationTimeDto,
        ThemeDto themeDto) {

    public ReservationDto(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getMember().getName().getName(),
                reservation.getDate(),
                new ReservationTimeDto(
                        reservation.getTime().getId(),
                        reservation.getTime().getStartAt()),
                new ThemeDto(reservation.getTheme().getId(),
                        reservation.getTheme().getName(),
                        reservation.getTheme().getDescription(),
                        reservation.getTheme().getThumbnail())
        );
    }
}
