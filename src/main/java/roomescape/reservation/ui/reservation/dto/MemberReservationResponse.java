package roomescape.reservation.ui.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.reservation.dto.ReservationMineInfo;

public record MemberReservationResponse(
        long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public MemberReservationResponse(final ReservationMineInfo reservationMineInfo) {
        this(reservationMineInfo.id(),
                reservationMineInfo.themeInfo().name(),
                reservationMineInfo.date(),
                reservationMineInfo.timeInfo().startAt(),
                reservationMineInfo.status()
        );
    }
}
