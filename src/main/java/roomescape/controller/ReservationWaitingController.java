package roomescape.controller;

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
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.response.ReservationWaitingResponse;
import roomescape.controller.dto.response.ReservationWaitingsResponse;
import roomescape.domain.ReservationWaiting;
import roomescape.service.ReservationWaitingService;

@RequestMapping("/reservations/waiting")
@RestController
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(@Valid @RequestBody ReservationRequest request) {
        ReservationWaiting waiting = reservationWaitingService.saveWaiting(
                request.name(), request.date(), request.timeId(), request.themeId());
        ReservationWaitingResponse response = ReservationWaitingResponse.from(waiting.reservation(), waiting.waitingNumber());
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable long id) {
        reservationWaitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ReservationWaitingsResponse> getReservationWaiting(@RequestParam String username) {
        List<ReservationWaitingResponse> responses = reservationWaitingService.findAllWaitingByName(username)
                .stream()
                .map(w -> ReservationWaitingResponse.from(w.reservation(), w.waitingNumber()))
                .toList();
        return ResponseEntity.ok(new ReservationWaitingsResponse(responses));
    }
}
