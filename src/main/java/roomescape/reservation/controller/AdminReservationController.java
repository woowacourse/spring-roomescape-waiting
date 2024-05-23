package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> save(
            @Valid @RequestBody AdminReservationSaveRequest adminRequest
    ) {
        ReservationResponse response = reservationService.saveByAdmin(adminRequest);

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }
}
