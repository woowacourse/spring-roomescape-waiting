package roomescape.reservationwaiting.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public AdminReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<ReservationResponse> approveWaiting(@PathVariable Long id) {
        ReservationResponse response = reservationWaitingService.approveWaiting(id);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }
}
