package roomescape.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RequestMapping("/reservations")
@RestController
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findByName(@RequestParam String name) {
        return ResponseEntity.ok(reservationService.findByName(name));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ReservationRequest request) {
        Long reservationId = reservationService.saveReservation(request);
        URI location = URI.create("/reservations/" + reservationId);
        return ResponseEntity
                .created(location)
                .build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable long id,
            @Valid @RequestBody ReservationRequest request
    ) {
        reservationService.updateReservation(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationService.cancelReservation(id, name);
        return ResponseEntity.noContent().build();
    }
}
