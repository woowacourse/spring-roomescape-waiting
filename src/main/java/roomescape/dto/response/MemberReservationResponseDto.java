package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.Reservation;
import roomescape.model.ReservationStatus;

public record MemberReservationResponseDto(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public MemberReservationResponseDto(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                ReservationStatus.RESERVED.getValue()
        );
    }
}
