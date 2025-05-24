package roomescape.admin.presentation;

import static roomescape.admin.presentation.AdminController.ADMIN_BASE_URL;
import static roomescape.member.presentation.MemberController.RESERVATION_BASE_URL;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping(ADMIN_BASE_URL)
public class AdminController {

    public static final String ADMIN_BASE_URL = "/admin";
    private static final String SLASH = "/";

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final AdminReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request.getReservationRequest(),
                request.memberId());
        URI locationUri = URI.create(RESERVATION_BASE_URL + SLASH + response.id());
        return ResponseEntity.created(locationUri).body(response);
    }

    @GetMapping("/waiting-reservations")
    public ResponseEntity<List<WaitingReservationResponse>> getWaitingReservation() {
        List<WaitingReservationResponse> waitingReservations = reservationService.getWaitingReservations();
        return ResponseEntity.ok(waitingReservations);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<Void> changeReservationStatusToReserved(@PathVariable("id") final Long id) {
        reservationService.changeWaitStatusToReserved(id);
        return ResponseEntity.ok().build();
    }
}
