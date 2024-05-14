package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.UserReservationSaveRequest;
import roomescape.infrastructure.Login;
import roomescape.service.ReservationService;
import roomescape.service.dto.LoginMember;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> saveReservation(
            @Login LoginMember member,
            @RequestBody @Valid UserReservationSaveRequest userReservationSaveRequest
    ) {
        ReservationSaveRequest reservationSaveRequest = userReservationSaveRequest.toReservationSaveRequest(member.id());
        ReservationResponse reservationResponse = reservationService.saveReservation(reservationSaveRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                    .body(reservationResponse);
    }
}
