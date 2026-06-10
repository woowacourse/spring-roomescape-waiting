package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.*;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> getAllReservations() {
        return ResponseEntity.ok(ReservationResponses.from(reservationService.findAllReservations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @GetMapping(params = {"userName"})
    public ResponseEntity<ReservationAndWaitingResponses> getReservationAndWaitingByName(@RequestParam String userName) {
        return ResponseEntity.ok(
                ReservationAndWaitingResponses.from(reservationService.findReservationAndWaitingByName(userName)));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationRequest request) {
        LocalDateTime requestTime = LocalDateTime.now();
        Reservation reservation = reservationService.saveReservation(
                request.name(), request.date(), request.timeId(), request.themeId(), requestTime
        );
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
        LocalDateTime requestTime = LocalDateTime.now();
        reservationService.removeReservation(id, userName, requestTime);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<ReservationResponse> patchReservation(
            @PathVariable
            long id,
            @RequestBody @Valid
            UpdateReservationRequest request,
            @RequestParam @NotBlank
            String userName
    ) {
        LocalDateTime requestTime = LocalDateTime.now();
        reservationService.updateReservation(
                id,
                userName,
                request.date(),
                request.timeId(),
                requestTime
        );
        return ResponseEntity.ok(ReservationResponse.from(reservationService.getReservationById(id)));
    }
}
