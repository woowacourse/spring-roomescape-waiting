package roomescape.waiting.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.waiting.domain.WaitingStatus;
import roomescape.waiting.dto.AdminWaitingResponse;
import roomescape.waiting.dto.AdminWaitingUpdateResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<AdminWaitingResponse>> getWatitings(Member member) {
        member.validateAdminOrThrow();
        List<AdminWaitingResponse> response = waitingService.getWaitings();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}")
    public ResponseEntity<AdminWaitingUpdateResponse> updateWaitingStatus(@RequestParam("status") WaitingStatus status,
                                                                          @PathVariable("id") Long waitingId,
                                                                          Member member) {
        member.validateAdminOrThrow();
        AdminWaitingUpdateResponse response = waitingService.updateWaitingStatus(status, waitingId);
        return ResponseEntity.ok(response);
    }
}
