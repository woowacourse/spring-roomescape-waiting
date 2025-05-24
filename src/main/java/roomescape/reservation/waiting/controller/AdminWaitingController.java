package roomescape.reservation.waiting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.service.WaitingService;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        List<WaitingResponse> responses = waitingService.getAllWaitings();
        return ResponseEntity.ok().body(responses);
    }
}
