package roomescape.reservationwaiting.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;

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
        return ResponseEntity.created(URI.create("/waitings/" + response.id())).body(response);
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
