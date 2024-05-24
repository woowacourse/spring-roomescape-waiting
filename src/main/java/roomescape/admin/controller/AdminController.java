package roomescape.admin.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.ReservationFilterRequest;
import roomescape.auth.annotation.Auth;
import roomescape.member.domain.MemberRole;
import roomescape.registration.reservation.dto.ReservationResponse;
import roomescape.registration.reservation.service.ReservationService;
import roomescape.registration.waiting.dto.WaitingResponse;
import roomescape.registration.waiting.service.WaitingService;

@RestController
@Auth(roles = MemberRole.ADMIN)
public class AdminController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<Void> reservationSave(@RequestBody AdminReservationRequest adminReservationRequest) {
        reservationService.addAdminReservation(adminReservationRequest);

        return ResponseEntity.created(URI.create("/admin/reservations/" + adminReservationRequest.memberId()))
                .build();
    }

    @GetMapping("/admin/reservations")
    public List<ReservationResponse> reservationFilteredList(
            @ModelAttribute ReservationFilterRequest reservationFilterRequest) {
        return reservationService.findFilteredReservations(reservationFilterRequest);
    }

    @GetMapping("/admin/waitings")
    public List<WaitingResponse> waitingList() {
        return waitingService.findWaitings();
    }

    @DeleteMapping("/admin/waitings/{waitingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void waitingReject(@PathVariable long waitingId) {
        waitingService.removeWaiting(waitingId);
    }
}
