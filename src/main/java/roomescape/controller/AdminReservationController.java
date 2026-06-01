package roomescape.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
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
