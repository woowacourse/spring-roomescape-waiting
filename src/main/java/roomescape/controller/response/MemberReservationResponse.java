package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.service.result.ReservationResult;

public record MemberReservationResponse(Long reservationId,
                                        String theme,
                                        LocalDate date,
                                        LocalTime time,
                                        String reservationStatus) {
    public static MemberReservationResponse from(ReservationResult reservationResult) {
        StringBuilder statusStringBuilder = new StringBuilder();
        if(reservationResult.waitingOrder() > 0) {
            statusStringBuilder.append(reservationResult.waitingOrder() + "번째 ");
        }
        statusStringBuilder.append(reservationResult.status().getName());

        return new MemberReservationResponse(reservationResult.id(), reservationResult.theme().name(),
                reservationResult.date(), reservationResult.time().startAt(),
                statusStringBuilder.toString());
    }

    public static List<MemberReservationResponse> from(List<ReservationResult> reservationResults) {
        return reservationResults.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
