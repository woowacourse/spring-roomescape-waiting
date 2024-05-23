package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Login;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.WaitingCreateRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
            @Valid @RequestBody WaitingCreateRequest waitingCreateRequest,
            @Login LoginMemberInToken loginMemberInToken
    ) {
        Waiting waiting = waitingService.save(waitingCreateRequest, loginMemberInToken);
        WaitingResponse waitingResponse = new WaitingResponse(waiting);

        return ResponseEntity.created(URI.create("/waiting/" + waiting.getId())).body(waitingResponse);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable Long id,
            @Login LoginMemberInToken loginMemberInToken
    ) {
        waitingService.delete(id, loginMemberInToken);
        return ResponseEntity.noContent().build();
    }
}
