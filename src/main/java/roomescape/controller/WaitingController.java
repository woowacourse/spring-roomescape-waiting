package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.controller.request.CreateWaitingRequest;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.WaitingResponse;
import roomescape.service.WaitingService;
import roomescape.service.result.WaitingResult;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(@LoginMember LoginMemberInfo loginMemberInfo,
                                                         @RequestBody CreateWaitingRequest createWaitingRequest) {
        WaitingResult waitingresult = waitingService.create(createWaitingRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(WaitingResponse.from(waitingresult));
    }
}
