package roomescape.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MemberReservationResponse(Long id,
                                        String theme,
                                        LocalDate date,
                                        LocalTime time,
                                        String status) {

    public static MemberReservationResponse fromWaitingReservation(Reservation reservation, int order) {
        return mapToMemberReservationResponse(reservation, String.format("%d번째 예약대기", order));
    }

    public static MemberReservationResponse fromConfirmedReservation(Reservation reservation) {
        return mapToMemberReservationResponse(reservation, reservation.getStatus().getValue());
    }

    private static MemberReservationResponse mapToMemberReservationResponse(Reservation reservation, String status) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status
        );
    }
}
