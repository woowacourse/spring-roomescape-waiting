package roomescape.waiting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.dto.response.AdminWaitingResponse;
import roomescape.waiting.service.WaitingServiceFacade;

import java.util.List;

@RestController
@RequestMapping("/admin/waiting")
public class AdminWaitingController {

    private final WaitingServiceFacade waitingService;

    public AdminWaitingController(WaitingServiceFacade waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<AdminWaitingResponse>> read() {
        List<AdminWaitingResponse> adminWaitingResponses = waitingService.getAdminWaitingResponses();
        return ResponseEntity.ok(adminWaitingResponses);
    }
}
