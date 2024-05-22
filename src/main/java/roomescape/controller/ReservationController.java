package roomescape.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.service.ReservationService;
import roomescape.service.dto.MemberReservation;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(
            @AuthenticationPrincipal Member member) {
        List<MemberReservation> memberReservations = reservationService.findMemberReservations(member);

        List<MemberReservationResponse> responses = MemberReservationResponse.of(memberReservations);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest request,
                                                         @AuthenticationPrincipal Member member) {
        Reservation reservation = reservationService.addReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<Reservation> createWaitingReservation(@RequestBody ReservationRequest request,
                                                                @AuthenticationPrincipal Member member) {
        Reservation reservation = reservationService.addWaitingReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }


    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
