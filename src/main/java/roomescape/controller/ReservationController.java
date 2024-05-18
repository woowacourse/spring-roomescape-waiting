package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.UserReservationSaveRequest;
import roomescape.infrastructure.Login;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.LoginMember;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.UserReservationResponse;

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
    public ResponseEntity<List<UserReservationResponse>> findAllUserReservation(@Login LoginMember member){
        List<UserReservationResponse> reservationResponses = reservationService.findAllUserReservation(member.id());
        return ResponseEntity.ok(reservationResponses);
    }
}
