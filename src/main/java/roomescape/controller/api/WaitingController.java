package roomescape.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.AuthMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.entity.Member;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    private WaitingController(
        WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponse createWaiting(
        @AuthMember Member member,
        @RequestBody WaitingRequest request) {
        return waitingService.addWaitingAfterNow(member, request);
    }
}
