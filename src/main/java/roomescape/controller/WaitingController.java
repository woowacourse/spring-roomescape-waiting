package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.infrastructure.MemberId;
import roomescape.service.WaitingService;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.UserReservationRequest;
import roomescape.service.dto.response.WaitingResponse;

@Controller
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> post(
            @RequestBody @Valid UserReservationRequest userReservationRequest,
            @MemberId Long id
    ) {
        ReservationRequest reservationRequest = userReservationRequest.toReservationRequest(id);
        WaitingResponse waitingResponse = waitingService.createWaiting(reservationRequest, id);
        URI location = UriComponentsBuilder.newInstance()
                .path("/{id}")
                .buildAndExpand(waitingResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyWaiting(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent()
                .build();
    }
}
