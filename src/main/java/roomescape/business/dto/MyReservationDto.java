package roomescape.business.dto;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.util.List;
import java.util.Map;

public record MyReservationDto(
        Id id,
        UserDto user,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme,
        int waitNumber
) {
    public static MyReservationDto fromEntry(final Reservation reservation, final int waitNumber) {
        return new MyReservationDto(
                reservation.getId(),
                UserDto.fromEntity(reservation.getUser()),
                reservation.getSlot().getDate(),
                ReservationTimeDto.fromEntity(reservation.getSlot().getTime()),
                ThemeDto.fromEntity(reservation.getSlot().getTheme()),
                waitNumber
        );
    }

    public static List<MyReservationDto> fromMap(final Map<Reservation, Integer> reservationsWithWaitNumber) {
        return reservationsWithWaitNumber.entrySet().stream()
                .map(entry -> fromEntry(entry.getKey(), entry.getValue()))
                .toList();
    }
}
