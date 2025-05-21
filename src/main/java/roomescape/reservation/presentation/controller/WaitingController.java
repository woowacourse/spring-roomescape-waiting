package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.member.presentation.resolver.LoginMember;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.presentation.dto.WaitingRequest;
import roomescape.reservation.presentation.dto.WaitingResponse;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @Auth(Role.USER)
    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(
            final @RequestBody @Valid WaitingRequest waitingRequest,
            final @LoginMember Long memberId
    ) {
        WaitingResponse waiting = waitingService.createWaiting(waitingRequest, memberId);

        return ResponseEntity.created(createUri(waiting.getId()))
                .body(waiting);
    }

    @Auth(Role.USER)
    @GetMapping
    public ResponseEntity<List<WaitingResponse>> getWaitings(
            final @LoginMember Long memberId
    ) {
        return ResponseEntity.ok().body(
                waitingService.getWaitings(memberId)
        );
    }

    @Auth(Role.USER)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(
            final @PathVariable Long id
    ) {
        System.out.println("=======================================id = " + id);
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

    private URI createUri(Long reservationId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservationId)
                .toUri();
    }
}