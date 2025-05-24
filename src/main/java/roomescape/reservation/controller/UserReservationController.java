package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateUserReservationRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
public class UserReservationController {

    private final ReservationService reservationService;

    public UserReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateUserReservationRequest request,
            @AuthMember final LoginMember member
    ) {
        final CreateReservationRequest newRequest = new CreateReservationRequest(
                request.date(), request.timeId(), request.themeId(), member.id()
        );
        final ReservationResponse response = reservationService.createReservation(newRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        final List<ReservationResponse> responses = reservationService.getAllReservations();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/mine/reservations")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(@AuthMember final LoginMember loginMember) {
        final List<MyReservationResponse> responses = reservationService.getMyReservations(loginMember);
        return ResponseEntity.ok().body(responses);
    }
}
