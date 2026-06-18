package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.repository.dto.ReservationWithRank;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationWithWaitingOrderResponse>> getAllByName(@RequestParam String name) {
        List<ReservationWithWaitingOrderResponse> body = reservationService.getAllByName(name);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam String name) {
        reservationService.cancelForUser(id, name);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UserReservationUpdateRequest request,
            @RequestParam String name) {
        ReservationResponse body = ReservationResponse.from(
                reservationService.update(id, request.timeId(), name));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationSaveResponse> create(
            @RequestBody @Valid ReservationSaveRequest reservationRequest) {
        ReservationSaveResponse body = ReservationSaveResponse.from(
                reservationService.create(reservationRequest.toServiceDto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/reservation-mine")
    public ResponseEntity<List<ReservationResponse>> findMine(@RequestParam String name) {
        List<ReservationResponse> response = reservationService.findMine(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservation-mine-rank")
    public ResponseEntity<List<ReservationWithRank>> findMineWithRank(@RequestParam String name) {
        List<ReservationWithRank> response = reservationService.findMineWithRank(name);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/waitings")
    public ResponseEntity<ReservationSaveResponse> createWaiting(@RequestBody @Valid ReservationSaveRequest request) {
        ReservationSaveResponse response = ReservationSaveResponse.from(
                reservationService.requestWaiting(request.toServiceDto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> cancelWaiting(@PathVariable Long id, @RequestParam String name) {
        reservationService.cancelWaiting(id, name);
        return ResponseEntity.noContent().build();
    }
}
