package roomescape.reservationhistory;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.reservationhistory.dto.ReservationHistoryResponse;

@RestController
@RequestMapping("/api/v1/reservation-history")
public class ReservationHistoryController {

    private final ReservationHistoryService reservationHistoryService;

    public ReservationHistoryController(ReservationHistoryService reservationHistoryService) {
        this.reservationHistoryService = reservationHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationHistoryResponse>> getMyHistory(@LoginMember Long memberId) {
        List<ReservationHistory> histories = reservationHistoryService.getMyHistory(memberId);
        return ResponseEntity.ok().body(ReservationHistoryResponse.fromAll(histories));
    }
}
