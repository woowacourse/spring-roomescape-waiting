package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.ReservationStatus;
import roomescape.service.result.ReservationWithWaitingResult;
import roomescape.service.result.WaitingWithRank;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String reservationStatus
) {
    public static MemberReservationResponse from(ReservationWithWaitingResult result) {
        return new MemberReservationResponse(
                result.reservationResult().id(),
                result.reservationResult().theme().name(),
                result.reservationResult().date(),
                result.reservationResult().time().startAt(),
                formatStatus(result.waitingWithRank())
        );
    }

    private static String formatStatus(WaitingWithRank waitingWithRank) {
        ReservationStatus status = waitingWithRank.reservationStatus();
        int rank = waitingWithRank.rank();

        StringBuilder sb = new StringBuilder();
        if(status == ReservationStatus.WAITING) {
            sb.append(rank + "번째 ");
        }
        sb.append(status.getDisplayName());

        return sb.toString();
    }

    public static List<MemberReservationResponse> from(List<ReservationWithWaitingResult> results) {
        return results.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
