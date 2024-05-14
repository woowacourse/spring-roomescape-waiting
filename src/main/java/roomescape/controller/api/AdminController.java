package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addAdminReservation(
            @RequestBody @Valid AdminReservationRequest request
    ) {
        ReservationResponse reservationResponse = reservationService.addReservation(
                request.date(),
                request.timeId(),
                request.themeId(),
                request.memberId()
        );

        return ResponseEntity.created(URI.create("/reservation/" + reservationResponse.id()))
                .body(reservationResponse);
    }
}
