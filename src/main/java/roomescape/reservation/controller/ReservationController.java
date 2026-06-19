package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.Reservation;
import roomescape.reservation.dto.ReservationChangeRequest;
import roomescape.reservation.dto.ReservationCreatedResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAll() {
        List<ReservationResponse> reservations = reservationService.findAll().stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(reservations);
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<ReservationResponse>> readAllByName(@RequestParam String name) {
        List<ReservationResponse> totalReservations = reservationService.findAllByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok().body(totalReservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> readById(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        return ResponseEntity.ok().body(ReservationResponse.from(reservation));
    }

    @PostMapping
    public ResponseEntity<ReservationCreatedResponse> create(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.add(
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId(),
                request.orderId(),
                request.amount()
        );

        URI location = URI.create("/reservations/" + reservation.getId());

        return ResponseEntity.created(location).body(new ReservationCreatedResponse(reservation.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateDateTimeByName(@PathVariable Long id,
                                                                    @Valid @RequestBody ReservationChangeRequest request) {
        Reservation reservation = reservationService.modifyDateTimeByName(
                id,
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()
        );

        return ResponseEntity.ok().body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}", params = "name")
    public ResponseEntity<Void> deleteByIdIfNameMatches(@PathVariable Long id, @RequestParam String name) {
        reservationService.deleteByIdIfNameMatches(id, name);
        return ResponseEntity.noContent().build();
    }
}
