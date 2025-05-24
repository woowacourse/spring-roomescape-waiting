package roomescape.business.dto;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.StartTime;
import roomescape.business.model.vo.ThemeName;
import roomescape.business.model.vo.UserName;

import java.util.List;

public record WaitingDto(
        Id reservationId,
        UserName userName,
        ThemeName themeName,
        ReservationDate date,
        StartTime time
) {

    public static WaitingDto fromEntity(final Reservation reservation) {
        return new WaitingDto(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getSlot().getTheme().getName(),
                reservation.getSlot().getDate(),
                reservation.getSlot().getTime().getStartTime()
        );
    }

    public static List<WaitingDto> fromEntities(final List<Reservation> reservations) {
        return reservations.stream()
                .map(WaitingDto::fromEntity)
                .toList();
    }
}
