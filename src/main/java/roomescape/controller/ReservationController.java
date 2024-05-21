package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> findAllReservations(@RequestParam(name = "waiting",defaultValue = "false")boolean isWaiting ) {
        if(isWaiting){
            return reservationService.findAllWaitings();
        }
        return reservationService.findAllReservations();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid ReservationRequest reservationRequest,
            @LoginMemberId long memberId) {
        ReservationResponse reservationResponse = reservationService.createMemberReservation(reservationRequest, memberId);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") long reservationId, @LoginMemberId long memberId) {
        reservationService.deleteWaitingById(reservationId, memberId);
        return ResponseEntity.noContent().build();
    }
}
