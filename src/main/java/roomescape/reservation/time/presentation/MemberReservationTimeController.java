package roomescape.reservation.time.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.time.application.ReservationTimeApplicationService;
import roomescape.reservation.time.presentation.dto.TimeConditionRequest;
import roomescape.reservation.time.presentation.dto.TimeConditionResponse;

@RestController
public class MemberReservationTimeController {

    private final ReservationTimeApplicationService reservationTimeApplicationService;

    public MemberReservationTimeController(ReservationTimeApplicationService reservationTimeApplicationService) {
        this.reservationTimeApplicationService = reservationTimeApplicationService;
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<TimeConditionResponse>> getReservationTimes(final TimeConditionRequest request) {
        List<TimeConditionResponse> responses = reservationTimeApplicationService.getTimesWithCondition(request);
        return ResponseEntity.ok().body(responses);
    }
}
