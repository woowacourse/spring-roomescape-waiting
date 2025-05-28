package roomescape.business.dto;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.StartTime;
import roomescape.business.model.vo.ThemeName;

import java.util.List;
import java.util.Map;

public record MyReservationDto(
        Id id,
        ThemeName themeName,
        ReservationDate date,
        StartTime time,
        int waitNumber
) {
    public static MyReservationDto fromEntry(final Reservation reservation, final int waitNumber) {
        return new MyReservationDto(
                reservation.getId(),
                reservation.getSlot().getTheme().getName(),
                reservation.getSlot().getDate(),
                reservation.getSlot().getTime().getStartTime(),
                waitNumber
        );
    }

    public static List<MyReservationDto> fromMap(final Map<Reservation, Integer> reservationsWithWaitNumber) {
        return reservationsWithWaitNumber.entrySet().stream()
                .map(entry -> fromEntry(entry.getKey(), entry.getValue()))
                .toList();
    }
}
