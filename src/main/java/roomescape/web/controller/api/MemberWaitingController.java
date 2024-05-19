package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.service.WaitingService;
import roomescape.service.request.WaitingAppRequest;
import roomescape.service.response.WaitingAppResponse;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMember;
import roomescape.web.controller.request.MemberWaitingWebRequest;
import roomescape.web.controller.response.WaitingWebResponse;

import java.net.URI;

@Controller
@RequestMapping("/waitings")
public class MemberWaitingController {

    private final WaitingService waitingService;

    public MemberWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingWebResponse> save(@Valid @RequestBody MemberWaitingWebRequest request,
                                                      @Valid @Auth LoginMember loginMember) {
        WaitingAppResponse waitingAppResponse = waitingService.save(
                new WaitingAppRequest(request.date(), request.timeId(),
                        request.themeId(), loginMember.id()));
        WaitingWebResponse waitingWebResponse = new WaitingWebResponse(waitingAppResponse);

        return ResponseEntity.created(URI.create("/waitings/" + waitingWebResponse.id()))
                .body(waitingWebResponse);
    }
}
