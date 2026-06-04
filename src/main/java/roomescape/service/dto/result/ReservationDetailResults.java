package roomescape.service.dto.result;

import java.util.List;

public record ReservationDetailResults(
        List<ReservationResult> reservationResults,
        List<WaitingDetailResult> waitingResults
){
}
