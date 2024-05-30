package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.MemberArgumentResolver;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.service.WaitingService;

import java.net.URI;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addWaiting(
            @RequestBody WaitingRequest waitingRequest,
            @MemberArgumentResolver Member member
    ) {
        ReservationResponse waitingResponse =
                waitingService.addWaiting(waitingRequest, member);

        return ResponseEntity.created(URI.create("/waiting/" + waitingResponse.id()))
                .body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaiting(
            @PathVariable("id") Long id,
            @MemberArgumentResolver Member member
    ) {
        waitingService.deleteById(id, member);
        return ResponseEntity.noContent().build();
    }
}
