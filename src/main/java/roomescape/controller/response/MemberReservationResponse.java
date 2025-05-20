package roomescape.controller.response;

import roomescape.service.result.ReservationResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MemberReservationResponse(Long reservationId,
                                        String theme,
                                        LocalDate date,
                                        LocalTime time,
                                        String status) {
    public static MemberReservationResponse from(ReservationResult reservationResult) {
        return new MemberReservationResponse(
                reservationResult.id(),
                reservationResult.theme().name(),
                reservationResult.date(),
                reservationResult.time().startAt(),
                reservationResult.status().getName());
    }

    public static List<MemberReservationResponse> from(List<ReservationResult> reservationResults) {
        return reservationResults.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
