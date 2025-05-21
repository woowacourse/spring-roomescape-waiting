package roomescape.member.ui.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.application.reservation.service.ReservationService;
import roomescape.reservation.ui.reservation.dto.AdminReservationCreateRequest;
import roomescape.reservation.ui.reservation.dto.ReservationResponse;

@RestController
@RequestMapping("/admin")
public class AdminApiController {

    private final ReservationService reservationService;

    public AdminApiController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid final AdminReservationCreateRequest request) {
        final ReservationInfo reservationInfo = reservationService.createReservation(request.convertToCreateCommand());
        final URI uri = URI.create("/reservations/" + reservationInfo.id());
        final ReservationResponse response = new ReservationResponse(reservationInfo);
        return ResponseEntity.created(uri).body(response);
    }
}
