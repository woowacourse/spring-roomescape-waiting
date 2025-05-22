package roomescape.waiting.presentation;

import java.net.URI;
import org.springframework.http.ResponseEntity;
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
}
