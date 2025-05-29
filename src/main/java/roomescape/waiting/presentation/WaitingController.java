package roomescape.waiting.presentation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.argumentResolver.Login;
import roomescape.member.dto.request.LoginMember;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
public class WaitingController {

    private WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> createWaiting(
            @RequestBody final WaitingRequest request,
            @Login final LoginMember loginMember
    ) {
        WaitingResponse response = waitingService.createWaiting(request, loginMember);
        URI location = URI.create("/waitings/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaitings() {
        List<WaitingResponse> waitings = waitingService.getAllWaitings();
        return ResponseEntity.ok(waitings);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> cancelWaiting(@PathVariable Long id) {
        waitingService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
