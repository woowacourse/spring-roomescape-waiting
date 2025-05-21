package roomescape.reservation.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.service.ReservationFacadeService;

@RestController
public class ReservationController {

    private final ReservationFacadeService reservationFacadeService;

    public ReservationController(final ReservationFacadeService reservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable("id") final Long id) {
        reservationFacadeService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
