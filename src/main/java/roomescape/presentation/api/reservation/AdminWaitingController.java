package roomescape.presentation.api.reservation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.WaitingService;
import roomescape.application.reservation.dto.WaitingResult;
import roomescape.presentation.api.reservation.response.WaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findWaitings() {
        List<WaitingResult> waitingResults = waitingService.findAllWaitings();
        List<WaitingResponse> waitingResponses = waitingResults.stream()
                .map(WaitingResponse::from)
                .toList();
        return ResponseEntity.ok(waitingResponses);
    }
}
