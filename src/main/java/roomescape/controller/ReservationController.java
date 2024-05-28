package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Login;
import roomescape.controller.dto.UserReservationSaveRequest;
import roomescape.controller.dto.UserReservationViewResponse;
import roomescape.controller.dto.UserReservationViewResponses;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.LoginMember;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(
            @Login LoginMember member,
            @RequestBody @Valid UserReservationSaveRequest userReservationSaveRequest
    ) {
        ReservationSaveRequest reservationSaveRequest = userReservationSaveRequest.toReservationSaveRequest(member.id());
        ReservationResponse reservationResponse = reservationService.saveReservation(reservationSaveRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<UserReservationViewResponses> findAllUserReservation(@Login LoginMember member) {
        List<UserReservationViewResponse> reservationResponses = reservationService.findAllUserReservation(member.id())
                .stream()
                .map(UserReservationViewResponse::from)
                .toList();
        UserReservationViewResponses response = new UserReservationViewResponses(reservationResponses);
        return ResponseEntity.ok(response);
    }
}
