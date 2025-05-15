package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.AuthenticationPrincipal;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservation.service.ReservationService;

@RestController
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
        ReservationResponse ReservationResponse = reservationService.add(
                memberResponse.id(),
                userReservationRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
