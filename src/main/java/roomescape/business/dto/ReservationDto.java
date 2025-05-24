package roomescape.business.dto;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.util.List;

public record ReservationDto(
        Id id,
        UserDto user,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme
) {
    public static ReservationDto fromEntity(final Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                UserDto.fromEntity(reservation.getUser()),
                reservation.getSlot().getDate(),
                ReservationTimeDto.fromEntity(reservation.getSlot().getTime()),
                ThemeDto.fromEntity(reservation.getSlot().getTheme())
        );
    }

    public static List<ReservationDto> fromEntities(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationDto::fromEntity)
                .toList();
    }
}
