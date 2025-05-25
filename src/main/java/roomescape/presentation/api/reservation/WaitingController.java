package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.WaitingService;
import roomescape.presentation.api.reservation.request.CreateWaitingRequest;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateWaitingRequest createWaitingRequest,
                                    @AuthPrincipal AuthInfo authInfo) {
        waitingService.create(createWaitingRequest.toServiceParam(authInfo.memberId()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<?> delete(@AuthPrincipal AuthInfo authInfo, @PathVariable("waitingId") Long waitingId) {
        waitingService.delete(waitingId);
        return ResponseEntity.noContent().build();
    }
}
