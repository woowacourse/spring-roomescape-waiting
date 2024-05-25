package roomescape.controller.reservation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.repository.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid final CreateReservationRequest request) {

        final Reservation reservation = reservationService.addReservation(request);
        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();

        return ResponseEntity.created(uri)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping("/admin/waitings")
    public List<WaitingReservationResponse> getWaitingReservations() {
        return reservationService.findAllWaiting();
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent()
                .build();
    }
}
