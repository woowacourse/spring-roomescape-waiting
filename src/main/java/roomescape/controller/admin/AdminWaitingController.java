package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.WaitingService;
import roomescape.service.dto.response.WaitingResponses;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {
    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping("/all")
    ResponseEntity<WaitingResponses> allWaitings() {
        WaitingResponses allWaitings = waitingService.findAllWaitings();
        return ResponseEntity.ok(allWaitings);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteWaiting(@RequestParam(name = "id") long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
