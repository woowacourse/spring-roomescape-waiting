package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.service.AuthService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationController(ReservationService reservationService, AuthService authService, ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.authService = authService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<Reservation> allReservations = reservationService.findAllReservations();
        List<ReservationResponse> responses = allReservations.stream()
                .map(ReservationResponse::new)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(HttpServletRequest request) {
        Long memberId = authService.findMemberIdByCookie(request.getCookies());
        List<MemberReservationResponse> responses = reservationWaitingService.getAllMemberReservationsAndWaiting(memberId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest request,
                                                         @AuthenticationPrincipal Member member) {
        Reservation reservation = reservationService.addReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long id) {
        reservationWaitingService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
