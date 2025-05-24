package roomescape.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.CreateWaitingRequest;
import roomescape.waiting.service.dto.CreateWaitingResponse;

@RestController
@RequestMapping("/waiting")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<CreateWaitingResponse> create(
            LoginMember loginMember,
            @Valid @RequestBody CreateWaitingRequest request
    ) {
        CreateWaitingResponse response = waitingService.createWaiting(request, loginMember);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
