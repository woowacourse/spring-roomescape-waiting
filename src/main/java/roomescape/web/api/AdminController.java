package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;

@RestController
@RequiredArgsConstructor
public class AdminController {
    private final ReservationService reservationService;

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.saveReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
                .body(ReservationResponse.from(reservation));
    }

    @PostMapping("/admin/reservations/{id}")
    public ResponseEntity<ReservationResponse> approveReservation(@PathVariable("id") Long id) {
        Reservation reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable("id") Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
