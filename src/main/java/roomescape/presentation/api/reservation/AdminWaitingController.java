package roomescape.presentation.api.reservation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.command.DeleteWaitingService;
import roomescape.application.reservation.command.WaitingPromotionService;
import roomescape.application.reservation.query.WaitingQueryService;
import roomescape.presentation.api.reservation.response.WaitingResponse;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingQueryService waitingQueryService;
    private final WaitingPromotionService waitingPromotionService;
    private final DeleteWaitingService deleteWaitingService;

    public AdminWaitingController(WaitingQueryService waitingQueryService,
                                  WaitingPromotionService waitingPromotionService,
                                  DeleteWaitingService deleteWaitingService) {
        this.waitingQueryService = waitingQueryService;
        this.waitingPromotionService = waitingPromotionService;
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

    @PostMapping("/{id}")
    public ResponseEntity<Void> approveWaiting(@AuthPrincipal AuthInfo authInfo, @PathVariable("id") Long waitingId) {
        waitingPromotionService.approve(waitingId, authInfo.memberId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@AuthPrincipal AuthInfo authInfo, @PathVariable("id") Long waitingId) {
        deleteWaitingService.cancel(waitingId, authInfo.memberId());
        return ResponseEntity.noContent().build();
    }
}
