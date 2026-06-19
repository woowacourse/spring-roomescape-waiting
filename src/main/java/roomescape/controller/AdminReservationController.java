package roomescape.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationWithWaitingOrder;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll() {
        LocalDateTime now = LocalDateTime.now();
        List<ReservationResponse> responses = reservationService.findAll().stream()
                .map(result -> toResponse(result, now))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody AdminReservationRequest request) {
        Long reservationId = reservationService.saveReservationByAdmin(request);
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

    private ReservationResponse toResponse(ReservationWithWaitingOrder result, LocalDateTime now) {
        return ReservationResponse.from(result.reservation(), result.order(), now);
    }
}
