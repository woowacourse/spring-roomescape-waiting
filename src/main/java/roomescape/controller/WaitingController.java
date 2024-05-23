package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.LoginMember;
import roomescape.dto.request.MemberWaitingRequest;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.WaitingService;

import java.net.URI;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaitingByClient(
            @Valid @RequestBody MemberWaitingRequest memberWaitingRequest, LoginMember member) {
        WaitingRequest waitingRequest = WaitingRequest.from (member.id(), memberWaitingRequest);
        WaitingResponse waitingResponse = waitingService.create(waitingRequest);

        return ResponseEntity.created(URI.create("/waiting/" + waitingResponse.id())).body(waitingResponse);
    }
}
