package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.WaitingRequest;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.Member;
import roomescape.model.Waiting;
import roomescape.service.WaitingService;

import java.net.URI;
import java.util.List;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<Waiting> createWaiting(@RequestBody WaitingRequest request,
                                                 @AuthenticationPrincipal Member member) {
        Waiting waiting = waitingService.addWaiting(request, member);
        return ResponseEntity.created(URI.create("/waiting/" + waiting.getId())).body(waiting);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaiting() {
        List<Waiting> waiting = waitingService.findAllWaiting();
        List<WaitingResponse> waitingResponses = waiting.stream()
                .map(WaitingResponse::new)
                .toList();
        return ResponseEntity.ok(waitingResponses);
    }
}
