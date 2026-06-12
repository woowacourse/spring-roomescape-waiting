package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        final List<ReservationResponse> results = reservationService.getAllReservations();
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{reservation-id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable("reservation-id") Long reservationId,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        final ReservationResponse result = reservationService.updateByAdmin(reservationId, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservation-id") Long reservationId
    ) {
        reservationService.cancelByAdmin(reservationId);
        return ResponseEntity.noContent().build();
    }
}
