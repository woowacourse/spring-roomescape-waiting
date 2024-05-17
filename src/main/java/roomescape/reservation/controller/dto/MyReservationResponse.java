package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.MemberReservation;

public record MyReservationResponse(long id, String themeName, LocalDate date, LocalTime time,
                                    String status) {
    public static MyReservationResponse from(MemberReservation memberReservation) {
        return new MyReservationResponse(
                memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(),
                memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(),
                memberReservation.getReservationStatus().getStatus()
        );
    }
}
