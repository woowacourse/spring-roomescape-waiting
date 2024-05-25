package roomescape.admin.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.ReservationFilterRequest;
import roomescape.auth.annotation.Auth;
import roomescape.member.domain.MemberRole;
import roomescape.registration.domain.reservation.dto.ReservationResponse;
import roomescape.registration.domain.reservation.service.ReservationService;
import roomescape.registration.domain.waiting.dto.WaitingResponse;
import roomescape.registration.domain.waiting.service.WaitingService;
import roomescape.registration.service.RegistrationService;

@RestController
@Auth(roles = MemberRole.ADMIN)
@RequestMapping("/admin")
public class AdminController {

    private final RegistrationService registrationService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminController(RegistrationService registrationService, ReservationService reservationService,
                           WaitingService waitingService) {
        this.registrationService = registrationService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<Void> reservationSave(@RequestBody AdminReservationRequest adminReservationRequest) {
        reservationService.addAdminReservation(adminReservationRequest);

        return ResponseEntity.created(URI.create("/admin/reservations/" + adminReservationRequest.memberId()))
                .build();
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> reservationFilteredList(
            @ModelAttribute ReservationFilterRequest reservationFilterRequest) {
        return reservationService.findFilteredReservations(reservationFilterRequest);
    }

    @GetMapping("/waitings")
    public List<WaitingResponse> waitingList() {
        return waitingService.findWaitings();
    }

    @DeleteMapping("/waitings/{waitingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void waitingReject(@PathVariable long waitingId) {
        waitingService.removeWaiting(waitingId);
    }

    @PatchMapping("/waitings/{waitingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void waitingApprove(@PathVariable long waitingId) {
        registrationService.approveWaitingToReservation(waitingId);
    }
}
