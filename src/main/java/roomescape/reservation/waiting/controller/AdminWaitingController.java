package roomescape.reservation.waiting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.service.WaitingService;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> approve(@PathVariable final long id) {
        final ReservationResponse response = waitingService.approveWaiting(id);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        final List<WaitingResponse> responses = waitingService.getAllWaitings();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deny(@PathVariable final long id) {
        waitingService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
