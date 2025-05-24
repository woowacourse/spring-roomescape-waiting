package roomescape.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.auth.annotation.RequiredAdmin;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RequestMapping("/admin/reservations/waiting")
@RestController
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @RequiredAdmin
    @GetMapping
    public ResponseEntity<List<WaitingResponse>> readAllWaiting() {
        List<WaitingResponse> responses = waitingService.getAll();

        return ResponseEntity.ok(responses);
    }
}
