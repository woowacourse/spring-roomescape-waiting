package roomescape.member.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record MemberReservationResponse(long id, String themeName, LocalDate date, LocalTime time, String status) {

    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getReservationTime().getStartAt(),
                reservation.getReservationStatus());
    }

    public static MemberReservationResponse from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();
        return new MemberReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getReservationTime().getStartAt(),
                waiting.getCount() + reservation.getReservationStatus());
    }
}
