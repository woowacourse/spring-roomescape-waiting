package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.request.UpdateReservationRequest;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.controller.dto.response.ReservationResponses;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping(params = "!name")
    public ResponseEntity<ReservationResponses> findReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Reservation> reservations = reservationService.findReservations(page, size);
        ReservationResponses response = ReservationResponses.from(reservations);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(params = "name")
    public ResponseEntity<ReservationResponses> findUserReservations(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Reservation> reservations = reservationService.findUserReservations(name, page, size);
        ReservationResponses response = ReservationResponses.from(reservations);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId());
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @RequestParam String name,
            @Valid @RequestBody UpdateReservationRequest request
    ) {
        Reservation reservation = reservationService.updateReservation(id, name, request.date(), request.timeId());
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @DeleteMapping(value = "/{id}", params = "name")
    public ResponseEntity<Void> deleteUserReservation(@PathVariable Long id, @RequestParam String name) {
        reservationService.deleteUserReservation(id, name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
