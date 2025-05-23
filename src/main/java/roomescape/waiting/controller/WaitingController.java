package roomescape.waiting.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.waiting.dto.CreateWaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @RequestBody @Valid final CreateWaitingRequest request,
            @AuthMember final LoginMember member
    ) {
        final WaitingResponse response = waitingService.createWaiting(request, member);
        return ResponseEntity.created(URI.create("/waitings/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") final long id,
            @AuthMember LoginMember loginMember
    ) {
        waitingService.deleteWaitingById(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
