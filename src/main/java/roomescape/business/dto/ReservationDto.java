package roomescape.business.dto;

import java.util.List;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.Status;

public record ReservationDto(
        Id id,
        UserDto user,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme,
        Status status
) {
    public static ReservationDto fromEntity(final Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                UserDto.fromEntity(reservation.getUser()),
                reservation.getDate(),
                ReservationTimeDto.fromEntity(reservation.getTime()),
                ThemeDto.fromEntity(reservation.getTheme()),
                reservation.getStatus()
        );
    }

    public static List<ReservationDto> fromEntities(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationDto::fromEntity)
                .toList();
    }
}
