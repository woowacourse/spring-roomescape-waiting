package roomescape.controller.admin.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.service.reservation.ReservationRegisterService;

import java.net.URI;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationRegisterController {
    private final ReservationRegisterService reservationRegisterService;

    public AdminReservationRegisterController(final ReservationRegisterService reservationRegisterService) {
        this.reservationRegisterService = reservationRegisterService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> register(@RequestBody ReservationRequest reservationRequest) {
        ReservationResponse reservationResponse = reservationRegisterService.register(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }
}
