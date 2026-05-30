package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.BookingResponse;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> reservations() {
        return ResponseEntity.ok(
                reservationService.allReservations().stream()
                        .map(ReservationResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable long id) {
        Reservation reservation = reservationService.findReservationById(id);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @GetMapping(params = {"userName"})
    public ResponseEntity<List<BookingResponse>> getReservationByName(@RequestParam String userName) {
        return ResponseEntity.ok(reservationService.findReservationByName(userName).stream()
                .map(BookingResponse::from)
                .toList()
        );
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationRequest request) {
        Reservation reservation = reservationService.saveReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
                .body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable
            long id,
            @RequestParam @NotBlank
            String userName
    ) {
        reservationService.removeReservation(id, userName);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable
            long id,
            @RequestParam @NotBlank
            String userName,
            @RequestBody @Valid
            ReservationRequest request
    ) {
        reservationService.putReservation(id, userName, request);
        return ResponseEntity.ok(ReservationResponse.from(reservationService.findReservationById(id)));
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<ReservationResponse> patchReservation(
            @PathVariable
            long id,
            @RequestBody
            ReservationPatchRequest request,
            @RequestParam @NotBlank
            String userName
    ) {
        reservationService.patchReservation(id, userName, request);
        return ResponseEntity.ok(ReservationResponse.from(reservationService.findReservationById(id)));
    }
}
