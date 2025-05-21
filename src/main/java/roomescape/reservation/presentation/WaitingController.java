package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.Authenticated;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RestController
@RequestMapping("/api/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaitList(@Authenticated Long memberId,
                                                       @RequestBody @Valid WaitingRequest request) {
        WaitingResponse response = waitingService.createWaiting(memberId, request);
        return ResponseEntity.created(URI.create("/api/waiting/" + response.id())).body(response);
    }
}
