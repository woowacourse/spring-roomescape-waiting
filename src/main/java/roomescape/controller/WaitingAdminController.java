package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
public class WaitingAdminController {

    private final WaitingService waitingService;

    public WaitingAdminController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/{waitingId}/approve")
    public ResponseEntity<Void> approveWaiting(@PathVariable Long waitingId) {
        waitingService.approve(waitingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{waitingId}/reject")
    public ResponseEntity<Void> rejectWaiting(@PathVariable Long waitingId) {
        waitingService.deleteById(waitingId);
        return ResponseEntity.noContent().build();
    }
}
