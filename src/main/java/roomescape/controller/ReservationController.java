package roomescape.controller;

import jakarta.validation.Valid;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.controller.dto.response.ReservationResponses;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Reservations;
import roomescape.service.ReservationService;

@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        Reservation reservation = reservationService.reserve(request, LocalDateTime.now());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();

        return ResponseEntity.created(location).body(ReservationResponse.toDto(reservation));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ReservationResponses> findList(@RequestParam(required = false) String name) {
        Reservations reservations = reservationService.findList(name);
        return ResponseEntity.ok(ReservationResponses.toDto(reservations));
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> find(@PathVariable long id) {
        Reservation reservation = reservationService.find(id);
        return  ResponseEntity.ok(ReservationResponse.toDto(reservation));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, @RequestParam String name) {
        reservationService.cancel(id, name, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> update(@Valid @RequestBody ReservationUpdateRequest request, @PathVariable long id) {
        Reservation updated = reservationService.update(request, id, LocalDateTime.now());
        return ResponseEntity.ok(ReservationResponse.toDto(updated));
    }
}
