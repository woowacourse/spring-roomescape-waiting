package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.reservation.Reservation;
import roomescape.service.ReservationCommandService;
import roomescape.service.ReservationQueryService;
import roomescape.web.dto.request.ReservationRequest;
import roomescape.web.dto.response.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<Reservation> allReservations = reservationQueryService.getAllReservations();

        List<ReservationResponse> reservationResponses = allReservations.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request
    ) {
        Reservation reservation = reservationCommandService.create(ReservationRequest.toCommand(request));

        Long savedId = reservation.getId();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        reservationCommandService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
