package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingReservationResponse>> getWaitings() {
        return ResponseEntity.ok(waitingService.findAllWaitings());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaitingByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
