package roomescape.presentation;

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
import roomescape.application.WaitingService;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.WaitingRequest;
import roomescape.application.dto.WaitingWithRankResponse;
import roomescape.application.dto.WaitingResponse;
import roomescape.infrastructure.authentication.AuthenticationPrincipal;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<List<WaitingWithRankResponse>> reserveWaiting(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody WaitingRequest request) {
        List<WaitingWithRankResponse> responses = waitingService.reserveWaiting(loginMember, request);
        return ResponseEntity.created(URI.create("/reservations/my")).body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        waitingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        List<WaitingResponse> responses = waitingService.findAll();
        return ResponseEntity.ok(responses);
    }
}
