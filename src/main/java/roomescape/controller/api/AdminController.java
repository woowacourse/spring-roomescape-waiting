package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<ReservationResponse>> getReservationWaitings() {
        List<ReservationResponse> reservationResponses = reservationService.getReservationWaitings();

        return ResponseEntity.ok(reservationResponses);
    }
}
