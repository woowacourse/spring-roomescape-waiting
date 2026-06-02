package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.WaitingResponseDTO;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/api/admin")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponseDTO>> readAll() {
        return ResponseEntity.ok(waitingService.readAllWaiting());
    }
}
