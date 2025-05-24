package roomescape.controller.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.dto.SessionMember;
import roomescape.service.WaitingService;
import roomescape.service.response.WaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAllWaitings() {
        return ResponseEntity.ok(waitingService.findAll());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveWaiting(@PathVariable final Long id) {
        waitingService.approveWaitingById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable final Long id,
                                              final SessionMember sessionMember) {
        waitingService.deleteWaitingById(id, sessionMember);
        return ResponseEntity.ok().build();
    }
}
