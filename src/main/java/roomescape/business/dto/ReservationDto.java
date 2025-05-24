package roomescape.business.dto;

import java.util.List;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.ReservationStatus;

public record ReservationDto(
        Id id,
        UserDto user,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme,
        ReservationStatus reservationStatus
) {
    public static ReservationDto fromEntity(final Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                UserDto.fromEntity(reservation.getUser()),
                reservation.getDate(),
                ReservationTimeDto.fromEntity(reservation.getTime()),
                ThemeDto.fromEntity(reservation.getTheme()),
                reservation.getReservationStatus()
        );
    }

    public static List<ReservationDto> fromEntities(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationDto::fromEntity)
                .toList();
    }
}
