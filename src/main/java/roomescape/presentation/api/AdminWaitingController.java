package roomescape.presentation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.presentation.dto.response.WaitingResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> getAllWaitings() {
        List<WaitingResponse> allWaitings = waitingService.getAllWaitings();
        return ResponseEntity.ok(allWaitings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> denyWaiting(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
