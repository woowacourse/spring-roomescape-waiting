package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Login;
import roomescape.service.WaitingService;
import roomescape.service.dto.request.LoginMember;
import roomescape.service.dto.request.WaitingRequest;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<Void> saveWaiting(
            @Login LoginMember member,
            @RequestBody @Valid WaitingRequest waitingRequest
    ){
        waitingService.saveWaiting(waitingRequest);
        return ResponseEntity.ok().build();
    }
}
