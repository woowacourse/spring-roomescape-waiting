package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> save(@Valid @RequestBody ReservationSaveRequest saveRequest) {
        ReservationResponse response = reservationService.save(saveRequest);

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }
}
