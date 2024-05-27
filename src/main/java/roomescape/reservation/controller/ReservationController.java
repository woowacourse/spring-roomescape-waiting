package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.MemberArgumentResolver;
import roomescape.member.dto.MemberRequest;
import roomescape.reservation.dto.ReservationOfMemberResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody ReservationRequest reservationRequest,
            @MemberArgumentResolver MemberRequest memberRequest
    ) {
        ReservationResponse reservationResponse =
                reservationService.addReservation(reservationRequest, memberRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        return ResponseEntity.ok(reservationService.findReservations());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationOfMemberResponse>> findReservationsByMember(
            @MemberArgumentResolver MemberRequest memberRequest) {
        return ResponseEntity.ok(reservationService.findReservationsByMember(memberRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id,
            @MemberArgumentResolver MemberRequest memberRequest) {
        reservationService.deleteReservation(id, memberRequest.toLoginMember());
        return ResponseEntity.noContent().build();
    }
}
