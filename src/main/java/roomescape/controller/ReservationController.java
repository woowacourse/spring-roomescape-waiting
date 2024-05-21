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
        Reservation reservation = reservationService.findById(id);
        reservationService.deleteReservation(id);
        if (waitingService.existsWaiting(reservation.getTheme(), reservation.getDate(), reservation.getTime())) {
            reservationService.addReservation(new ReservationRequest(
                    reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()),
                    reservation.getMember()
            );
            Waiting waiting = waitingService.findFirstWaitingByCondition(reservation.getTheme(), reservation.getDate(), reservation.getTime());
            waitingService.deleteWaiting(waiting.getId());
        }
        return ResponseEntity.noContent().build();
    }
}
