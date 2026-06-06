package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record UserReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        Long waitingNumber
) {

    public static UserReservationResponse confirmed(Reservation reservation) {
        Slot slot = reservation.getSlot();
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ThemeResponse.from(slot.getTheme()),
                reservation.getStatus().name(),
                null
        );
    }

    public static UserReservationResponse waiting(WaitingRank waitingRank) {
        Reservation reservation = waitingRank.reservation();
        Slot slot = reservation.getSlot();
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ThemeResponse.from(slot.getTheme()),
                reservation.getStatus().name(),
                waitingRank.waitingNumber()
        );
    }
}
