package roomescape.waiting.controller;

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
import roomescape.waiting.dto.request.CreateWaitingRequest;
import roomescape.waiting.dto.response.CreateWaitingResponse;
import roomescape.waiting.dto.response.FindWaitingWithRankingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<CreateWaitingResponse> createWaiting(@AuthenticationPrincipal AuthInfo authInfo,
                                                               @Valid @RequestBody CreateWaitingRequest createWaitingRequest) {
        CreateWaitingResponse createWaitingResponse = waitingService.createWaiting(authInfo, createWaitingRequest);
        return ResponseEntity.created(URI.create("/waitings/" + createWaitingResponse.waitingId()))
                .body(createWaitingResponse);
    }

    @DeleteMapping("/waitings/{waitingId}")
    public ResponseEntity<Void> deleteWaiting(@AuthenticationPrincipal AuthInfo authInfo,
                                                               @PathVariable Long waitingId) {
        waitingService.deleteWaiting(authInfo, waitingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members/waitings")
    public ResponseEntity<List<FindWaitingWithRankingResponse>> getWaitingsByMember(
            @AuthenticationPrincipal AuthInfo authInfo) {
        return ResponseEntity.ok(waitingService.getWaitingsByMember(authInfo));
    }
}
