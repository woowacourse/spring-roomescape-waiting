package roomescape.presentation.api.reservation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.command.DeleteWaitingService;
import roomescape.application.reservation.query.WaitingQueryService;
import roomescape.presentation.api.reservation.response.WaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingQueryService waitingQueryService;
    private final DeleteWaitingService deleteWaitingService;

    public AdminWaitingController(WaitingQueryService waitingQueryService, DeleteWaitingService deleteWaitingService) {
        this.waitingQueryService = waitingQueryService;
        this.deleteWaitingService = deleteWaitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        List<WaitingResponse> waitingResponses = waitingQueryService.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
        return ResponseEntity.ok(waitingResponses);
    }
}
