package roomescape.controller.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
        return ResponseEntity.ok(waitingService.findEntireWaitingList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        waitingService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
