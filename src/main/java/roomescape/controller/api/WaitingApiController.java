package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.controller.api.dto.request.ReservationRequest;
import roomescape.controller.api.dto.response.ReservationResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.output.WaitingOutput;

import java.net.URI;

@Controller
@RequestMapping("/waitings")
public class WaitingApiController {
    private final WaitingService waitingService;

    public WaitingApiController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createWaiting(@RequestBody final ReservationRequest reservationRequest,
                                                             final LoginMemberRequest loginMemberRequest) {
        final WaitingOutput output = waitingService.createWaiting(reservationRequest.toInput(loginMemberRequest.id()));
        return ResponseEntity.created(URI.create("/waitings/" + output.id()))
                .body(ReservationResponse.toResponse(output));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable final long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent()
                .build();
    }
}
