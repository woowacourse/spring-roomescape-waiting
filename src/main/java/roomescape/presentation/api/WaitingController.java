package roomescape.presentation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.presentation.AuthenticationPrincipal;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.WaitingRequest;
import roomescape.presentation.dto.response.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
            @RequestBody WaitingRequest waitingRequest,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        WaitingResponse response = waitingService.createWaiting(waitingRequest, loginMember);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
