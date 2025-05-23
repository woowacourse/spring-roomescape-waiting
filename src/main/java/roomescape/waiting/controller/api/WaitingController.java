package roomescape.waiting.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.waiting.controller.dto.request.WaitingRequest;
import roomescape.waiting.controller.dto.response.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponse createReservationWaiting(
            @AuthMember Member member,
            @RequestBody @Valid WaitingRequest request
    ) {
        return WaitingResponse.from(waitingService.addWaiting(member, request));
    }


}
