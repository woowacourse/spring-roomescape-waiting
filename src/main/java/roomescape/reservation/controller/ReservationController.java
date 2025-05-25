package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationWithMemberRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationCommandService;
import roomescape.reservation.service.ReservationQueryService;

@RestController
public class ReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    public ReservationController(final ReservationCommandService reservationCommandService,
                                 final ReservationQueryService reservationQueryService) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateReservationRequest request,
            @AuthMember final LoginMember member
    ) {
        final CreateReservationWithMemberRequest newRequest = new CreateReservationWithMemberRequest(
                request.date(), request.timeId(), request.themeId(), member.id());

        final ReservationResponse response = reservationCommandService.createReservation(newRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        final List<ReservationResponse> responses = reservationQueryService.getReservations();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/me/reservations")
    public ResponseEntity<List<MyReservationResponse>> findAllMyReservations(@AuthMember LoginMember loginMember) {
        final List<MyReservationResponse> responses = reservationQueryService.getMyReservations(loginMember);
        return ResponseEntity.ok().body(responses);
    }
}
