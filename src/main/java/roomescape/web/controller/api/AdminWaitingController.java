package roomescape.web.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.WaitingService;
import roomescape.web.controller.response.WaitingWebResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingWebResponse>> getWaitings() {
        List<WaitingWebResponse> waitingWebResponses = waitingService.findAll()
                .stream()
                .map(WaitingWebResponse::new)
                .toList();

        return ResponseEntity.ok().body(waitingWebResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
