package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.core.AuthenticationPrincipal;
import roomescape.auth.domain.AuthInfo;
import roomescape.reservation.dto.request.CreateWaitingRequest;
import roomescape.reservation.dto.response.CreateWaitingResponse;
import roomescape.reservation.dto.response.FindWaitingResponse;
import roomescape.reservation.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<CreateWaitingResponse> createWaiting(
            @AuthenticationPrincipal AuthInfo authInfo,
            @Valid @RequestBody CreateWaitingRequest createWaitingRequest) {
        CreateWaitingResponse createWaitingResponse =
                waitingService.createWaiting(authInfo, createWaitingRequest);
        return ResponseEntity.created(URI.create("/waitings/" + createWaitingResponse.id()))
                .body(createWaitingResponse);
    }

    @GetMapping
    public ResponseEntity<List<FindWaitingResponse>> getWaitings() {
        return ResponseEntity.ok(waitingService.getWaitings());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
