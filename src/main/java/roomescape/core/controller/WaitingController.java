package roomescape.core.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.waiting.MemberWaitingRequest;
import roomescape.core.dto.waiting.WaitingRequest;
import roomescape.core.dto.waiting.WaitingResponse;
import roomescape.core.service.WaitingService;

@RestController
@RequestMapping("/waiting")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(@Valid @RequestBody final MemberWaitingRequest request,
                                                  final LoginMember loginMember) {
        final WaitingRequest waitingRequest = new WaitingRequest(loginMember.getId(), request.getDate(),
                request.getTimeId(),
                request.getThemeId());

        final WaitingResponse result = waitingService.create(waitingRequest);
        return ResponseEntity.created(URI.create("/waiting/" + result.getId()))
                .body(result);
    }
}
