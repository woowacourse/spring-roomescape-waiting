package roomescape.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.global.AdminOnly;
import roomescape.service.ReservationService;

@AdminOnly
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
    public ResponseEntity<Void> create(@Valid @RequestBody AdminReservationRequest request) {
        Long reservationId = reservationService.saveReservation(request);
        URI location = URI.create("/reservations/" + reservationId);
        return ResponseEntity
                .created(location)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancelReservationByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
