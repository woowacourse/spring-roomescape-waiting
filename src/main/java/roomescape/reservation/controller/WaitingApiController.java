package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Login;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RestController
public class WaitingApiController {

    private final WaitingService waitingService;

    public WaitingApiController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
            @Valid @RequestBody WaitingCreateRequest request,
            @Login LoginMemberInToken loginMember
    ) {
        Waiting waiting = waitingService.save(request, loginMember);
        WaitingResponse waitingResponse = new WaitingResponse(waiting);

        return ResponseEntity.created(URI.create("/waiting/" + waiting.getId())).body(waitingResponse);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> findAll(@Login LoginMemberInToken loginMember) {
        List<WaitingResponse> waitingResponses = waitingService.findAll(loginMember);

        return ResponseEntity.ok(waitingResponses);
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
