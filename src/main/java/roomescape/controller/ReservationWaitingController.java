package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationWaitingService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @RequestBody @Valid ReservationRequest waitingRequest,
            @LoginMemberId long memberId
    ) {
        ReservationWaitingResponse resepone = reservationWaitingService.create(waitingRequest, memberId);

        return ResponseEntity.created(URI.create("/reservations/waiting/" + resepone.id()))
                .body(resepone);
    }
}
