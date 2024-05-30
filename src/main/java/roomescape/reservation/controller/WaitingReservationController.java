package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationFacadeService;

@RestController
@RequestMapping("/waiting-reservations")
public class WaitingReservationController {
    private final ReservationFacadeService reservationFacadeService;

    public WaitingReservationController(ReservationFacadeService reservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservationWaitings() {
        List<ReservationResponse> response = reservationFacadeService.findReservationWaitings();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createWaitingReservation(ReservationCreateRequest request) {
        ReservationResponse reservationCreateResponse = reservationFacadeService.createWaitingReservation(request);

        URI uri = URI.create("/waiting-reservations/" + reservationCreateResponse.id());
        return ResponseEntity.created(uri)
                .body(reservationCreateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable long id) {
        reservationFacadeService.deleteReservationWaiting(id);

        return ResponseEntity.noContent()
                .build();
    }
}
