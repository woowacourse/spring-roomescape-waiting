package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.AuthenticationPrincipal;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservation.service.ReservationService;

@Controller
@RequestMapping("/reservations")
public class ReservationApiController {
    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(
            @RequestBody UserReservationRequest userReservationRequest,
            @AuthenticationPrincipal MemberResponse memberResponse
    ) {
        ReservationResponse ReservationResponse = reservationService.addByUser(
                memberResponse.id(),
                userReservationRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> addWaiting(
            @RequestBody UserReservationRequest userReservationRequest,
            @AuthenticationPrincipal MemberResponse memberResponse
    ) {
        ReservationResponse ReservationResponse = reservationService.addWaiting(
                memberResponse.id(),
                userReservationRequest
        );
        return ResponseEntity.ok(ReservationResponse);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ReservationResponse> addFromWaiting(@PathVariable("id") Long id) {
        ReservationResponse reservationResponse = reservationService.addFromWaiting(id);
        return ResponseEntity.ok(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
