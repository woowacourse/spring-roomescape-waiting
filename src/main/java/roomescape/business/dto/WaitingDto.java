package roomescape.business.dto;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
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

    public static WaitingDto fromEntity(final ReservationSlot slot, final Reservation reservation) {
        return new WaitingDto(
                reservation.getId(),
                reservation.getUser().getName(),
                slot.getTheme().getName(),
                slot.getDate(),
                slot.getTime().getStartTime()
        );
    }

    public static List<WaitingDto> fromEntities(final List<ReservationSlot> slots) {
        return slots.stream()
                .flatMap(slot -> slot.getReservations().stream()
                        .map(reservation -> WaitingDto.fromEntity(slot, reservation))
                )
                .toList();
    }
}
