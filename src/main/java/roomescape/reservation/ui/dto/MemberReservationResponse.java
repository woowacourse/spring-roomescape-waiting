package roomescape.reservation.ui.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.ReservationInfo;

public record MemberReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public MemberReservationResponse(final ReservationInfo reservationInfo) {
        this(reservationInfo.id(),
                reservationInfo.theme().name(),
                reservationInfo.date(),
                reservationInfo.time().startAt(),
                reservationInfo.status().getDisplayName()
        );
    }
}
