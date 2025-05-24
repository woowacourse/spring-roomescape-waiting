package roomescape.reservation.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.waiting.dto.CreateUserWaitingRequest;
import roomescape.reservation.waiting.dto.CreateWaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.service.WaitingService;

import java.net.URI;

@RestController
@RequestMapping("/reservations/waitings")
public class UserWaitingController {

    private final WaitingService waitingService;

    public UserWaitingController(final WaitingService waitingService) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        waitingService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
