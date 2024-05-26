package roomescape.reservation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.global.annotation.Auth;
import roomescape.member.role.MemberRole;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.AdminWaitingResponse;
import roomescape.reservation.dto.ReservationFilterRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.facade.ReservationFacadeService;

@RestController
public class ReservationController {

    private final ReservationFacadeService reservationService;

    public ReservationController(ReservationFacadeService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reservationSave(@RequestBody ReservationRequest reservationRequest,
                                                               @LoginMemberId long id) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.addReservation(reservationRequest, id));
    }

    @Auth(roles = MemberRole.ADMIN)
    @PostMapping("/admin/reservations")
    public ResponseEntity<Void> reservationSave(@RequestBody AdminReservationRequest adminReservationRequest) {
        reservationService.addAdminReservation(adminReservationRequest);

        return ResponseEntity.created(URI.create("/admin/reservations/" + adminReservationRequest.memberId()))
                .build();
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<ReservationResponse> reservationWaitingSave(
            @RequestBody ReservationRequest reservationRequest,
            @LoginMemberId long id) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.addWaitingReservation(reservationRequest, id));
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> reservaionList() {
        return reservationService.findReservations();
    }

    @GetMapping("/reservations/{themeId}")
    public List<ReservationTimeAvailabilityResponse> reservationTimeList(@PathVariable long themeId,
                                                                         @RequestParam LocalDate date) {
        return reservationService.findTimeAvailability(themeId, date);
    }

    @Auth(roles = MemberRole.ADMIN)
    @GetMapping("/admin/reservations")
    public List<ReservationResponse> reservationFilteredList(
            @ModelAttribute ReservationFilterRequest reservationFilterRequest) {
        return reservationService.findFilteredReservations(reservationFilterRequest);
    }

    @Auth(roles = MemberRole.ADMIN)
    @GetMapping("/admin/waitings")
    public List<AdminWaitingResponse> waitingList() {
        return reservationService.findAdminWaitings();
    }

    @DeleteMapping("/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reservationRemove(@PathVariable long reservationId) {
        reservationService.removeReservation(reservationId);
    }

    @DeleteMapping("/reservations/waiting/{waitingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reservationWaitingRemove(@PathVariable long waitingId) {
        reservationService.removeWaitingReservation(waitingId);
    }
}
