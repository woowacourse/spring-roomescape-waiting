package roomescape.web.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.dto.login.LoginMember;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.service.booking.reservation.ReservationService;
import roomescape.service.booking.waiting.WaitingService;

@Controller
public class WaitingController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public WaitingController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<ReservationResponse> addReservationWaiting(
            @RequestBody UserReservationRequest userReservationRequest,
            LoginMember loginMember) {
        ReservationRequest reservationRequest = ReservationRequest.from(userReservationRequest, loginMember.id());
        Long savedId = waitingService.resisterWaiting(reservationRequest);
        ReservationResponse reservationResponse = reservationService.findReservation(savedId);
        return ResponseEntity.created(URI.create("/reservations/" + savedId)).body(reservationResponse);
    }

    @GetMapping("/admin/reservations/waiting")
    public ResponseEntity<List<WaitingResponse>> getWaitingReservations() {
        List<WaitingResponse> waitingResponse = waitingService.findAllWaitingReservations();
        return ResponseEntity.ok(waitingResponse);
    }

    @DeleteMapping("/admin/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingByAdmin(@PathVariable Long id) {
        waitingService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingByUser(@PathVariable Long id) {
        waitingService.cancelWaitingForUser(id);
        return ResponseEntity.noContent().build();
    }
}
