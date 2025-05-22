package roomescape.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.reservation.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time
) {

    public static MemberReservationResponse of(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getSchedule().getTheme().getName(),
                reservation.getSchedule().getDate(),
                reservation.getSchedule().getReservationTime().getStartAt()
        );
    }
}
