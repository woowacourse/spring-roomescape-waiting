package roomescape.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dto.ReservationWaitingRequest;
import roomescape.waiting.dto.ReservationWaitingResponse;
import roomescape.waiting.service.ReservationWaitingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservation-waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<ReservationWaitingResponse>> findByName(@RequestParam String name) {
        List<ReservationWaitingResponse> reservationWaitingResponses = reservationWaitingService.findByName(name).stream()
                .map(ReservationWaitingResponse::from)
                .toList();
        return ResponseEntity.ok().body(reservationWaitingResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationWaitingResponse> readById(@PathVariable Long id) {
        ReservationWaiting reservationWaiting = reservationWaitingService.findById(id);
        return ResponseEntity.ok().body(ReservationWaitingResponse.from(reservationWaiting));
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> create(@Valid @RequestBody ReservationWaitingRequest request) {
        ReservationWaiting reservationWaiting = reservationWaitingService.add(
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()
        );

        URI location = URI.create("/reservation-waitings/" + reservationWaiting.getId());

        return ResponseEntity.created(location).body(ReservationWaitingResponse.from(reservationWaiting));
    }

    @DeleteMapping(value = "/{id}", params = "name")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @Valid @RequestParam String name) {
        reservationWaitingService.deleteByIdIfNameMatches(id, name);
        return ResponseEntity.noContent().build();
    }
}
