package roomescape.admin.presentation;

import static roomescape.admin.presentation.AdminController.ADMIN_BASE_URL;
import static roomescape.member.presentation.MemberController.RESERVATION_BASE_URL;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping(ADMIN_BASE_URL)
public class AdminController {

    public static final String ADMIN_BASE_URL = "/admin";
    private static final String SLASH = "/";

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final AdminReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request.getReservationRequest(),
                request.memberId());
        URI locationUri = URI.create(RESERVATION_BASE_URL + SLASH + response.id());
        return ResponseEntity.created(locationUri).body(response);
    }


    @PostMapping("/waiting/{id}/approve")
    public ResponseEntity<Void> approveWaiting(@PathVariable Long id) {
        waitingService.approveWaiting(id);
        return ResponseEntity.ok().build();
    }
}
