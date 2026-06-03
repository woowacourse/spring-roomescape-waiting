package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationWithWaitingOrderResponse>> getAllByName(@RequestParam String name) {
        List<ReservationWithWaitingOrderResponse> body = reservationService.getAllByName(name);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam String name) {
        reservationService.cancelForUser(id, name);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UserReservationUpdateRequest request,
            @RequestParam String name) {
        ReservationResponse body = ReservationResponse.from(
                reservationService.update(id, request.timeId(), name));
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ReservationSaveResponse> create(
            @RequestBody @Valid ReservationSaveRequest reservationRequest) {
        ReservationSaveResponse body = ReservationSaveResponse.from(
                reservationService.create(reservationRequest.toServiceDto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
