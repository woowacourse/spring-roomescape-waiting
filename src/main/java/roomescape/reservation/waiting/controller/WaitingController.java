package roomescape.reservation.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.waiting.dto.CreateUserWaitingRequest;
import roomescape.reservation.waiting.dto.CreateWaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.service.WaitingService;

import java.net.URI;

@RestController
@RequestMapping("/reservations/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @RequestBody @Valid final CreateUserWaitingRequest request,
            @AuthMember final LoginMember member
    ) {
        final CreateWaitingRequest newRequest = new CreateWaitingRequest(
                request.date(), request.timeId(), request.themeId(), member.id()
        );
        final WaitingResponse response = waitingService.createWaiting(newRequest);
        return ResponseEntity.created(URI.create("/reservations/waitings/" + response.id())).body(response);
    }
}
