package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.MemberArgumentResolver;
import roomescape.member.dto.MemberRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.service.WaitingService;

import java.net.URI;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> addWaiting(
            @RequestBody WaitingRequest waitingRequest,
            @MemberArgumentResolver MemberRequest memberRequest
    ) {
        ReservationResponse waitingResponse =
                waitingService.addWaiting(waitingRequest, memberRequest);

        return ResponseEntity.created(URI.create("/waiting/" + waitingResponse.id()))
                .body(waitingResponse);
    }
}
