package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.reservation.dto.AdminCreateReservationRequest;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid final AdminCreateReservationRequest adminRequest) {
        CreateReservationRequest reservationRequest = new CreateReservationRequest(
                adminRequest.date(), adminRequest.timeId(), adminRequest.themeId());
        final ReservationResponse reservation = reservationService
                .addReservedReservation(reservationRequest, adminRequest.memberId());

        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.id())
                .toUri();
        return ResponseEntity.created(uri).body(reservation);
    }
}
