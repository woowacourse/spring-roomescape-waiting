package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class ReservationAdminController {

    private final ReservationService reservationService;

    public ReservationAdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> responses = reservationService.findReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

}
