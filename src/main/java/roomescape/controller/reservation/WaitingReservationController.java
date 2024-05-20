package roomescape.controller.reservation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/waiting")
public class WaitingReservationController {

    private final ReservationService reservationService;

    public WaitingReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getWaitingReservations() {
        return reservationService.getWaitingReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping("/{id}")
    public ResponseEntity<ReservationResponse> changeWaitingReservationToReserved(
            @PathVariable("id") final Long id) {
        Reservation reservation = reservationService.setWaitingReservationReserved(id);
        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();
        return ResponseEntity.created(uri)
                .body(ReservationResponse.from(reservation));
    }
}
