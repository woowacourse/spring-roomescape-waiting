package roomescape.controller.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.WaitingResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {
    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> read() {
        return ResponseEntity.ok(waitingService.findNotRejectedWaitingList());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WaitingResponse> patch(@PathVariable Long id) {
        WaitingResponse response = waitingService.rejectedByAdmin(id);
        return ResponseEntity.ok(response);
    }
}
