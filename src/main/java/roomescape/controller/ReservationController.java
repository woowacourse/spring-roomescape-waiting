package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.request.WaitingRequest;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.service.AuthService;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, WaitingService waitingService, AuthService authService) {
        this.reservationService = reservationService;
        this.authService = authService;
        this.waitingService = waitingService;
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
        List<Reservation> memberReservations = reservationService.findMemberReservations(memberId);
        List<WaitingWithRank> waitingWithRanks = waitingService.findMemberWaiting(memberId);
        List<MemberReservationResponse> responses =
                new java.util.ArrayList<>(memberReservations.stream()
                        .map(MemberReservationResponse::new)
                        .toList());
        List<MemberReservationResponse> waiting = waitingWithRanks.stream()
                .map(MemberReservationResponse::new)
                .toList();
        responses.addAll(waiting);
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
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/waiting")
    public ResponseEntity<Waiting> createWaiting(@RequestBody WaitingRequest request,
                                                 @AuthenticationPrincipal Member member) {
        Waiting waiting = waitingService.addWaiting(request, member);
        return ResponseEntity.created(URI.create("/waiting/" + waiting.getId())).body(waiting);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
