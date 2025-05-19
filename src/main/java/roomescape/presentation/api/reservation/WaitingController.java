package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.WaitingService;
import roomescape.presentation.api.reservation.request.CreateWaitingRequest;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations/wait")
    public ResponseEntity<Void> createWaiting(@AuthPrincipal AuthInfo authInfo,
                                        @Valid @RequestBody CreateWaitingRequest createWaitingRequest) {
        Long id = waitingService.create(createWaitingRequest.toCreateParameter(authInfo.memberId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/reservations/wait/" + id))
                .build();
    }
}
