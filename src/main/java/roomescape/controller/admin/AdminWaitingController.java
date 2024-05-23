package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.WaitingService;
import roomescape.service.dto.response.WaitingResponses;

@RestController
@RequestMapping("/admin")
public class AdminWaitingController {
    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping("/all-waitings")
    ResponseEntity<WaitingResponses> allWaitings() {
        WaitingResponses allWaitings = waitingService.findAllWaitings();
        return ResponseEntity.ok(allWaitings);
    }
}
