package roomescape.reservation.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RequestMapping("/reservations/waiting")
@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @Valid @RequestBody final ReservationRequest request,
            final LoginMember loginMember
    ) {
        WaitingCreateRequest createRequest = WaitingCreateRequest.from(request, loginMember);
        WaitingResponse response = waitingService.createWaiting(createRequest);

        return ResponseEntity.created(URI.create("/waiting/" + response.id()))
                .body(response);
    }
}
