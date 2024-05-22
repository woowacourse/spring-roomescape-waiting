package roomescape.web.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.login.LoginMember;
import roomescape.dto.waiting.WaitingRequest;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waitings")
class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<Void> addWaiting(@RequestBody WaitingRequest waitingRequest) {
        Long savedId = waitingService.addWaiting(waitingRequest);

        return ResponseEntity.created(URI.create("/waitings/" + savedId)).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id, LoginMember loginMember) {
        waitingService.deleteWaiting(id, loginMember);

        return ResponseEntity.noContent().build();
    }
}
