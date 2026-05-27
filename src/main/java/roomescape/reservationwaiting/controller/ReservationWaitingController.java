package roomescape.reservationwaiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createWaiting(
            @Valid @RequestBody ReservationWaitingRequest request) {
        ReservationWaitingResponse response = reservationWaitingService.createWaiting(request);
        return ResponseEntity.created(URI.create("/waiting/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingTurnResponse>> getWaitingByName(@RequestParam String name) {
        return ResponseEntity.ok(reservationWaitingService.getWaitingByName(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationWaitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
