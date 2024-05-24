package roomescape.web.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.login.LoginMember;
import roomescape.dto.waiting.UserWaitingRequest;
import roomescape.dto.waiting.WaitingRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.service.WaitingService;

@RestController
class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<Void> addWaiting(
            @RequestBody UserWaitingRequest userWaitingRequest,
            LoginMember loginMember
    ) {
        Long savedId = waitingService.addWaiting(WaitingRequest.from(userWaitingRequest, loginMember.id()));
        return ResponseEntity.created(URI.create("/waitings/" + savedId)).build();
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaitingByUser(@PathVariable Long id, LoginMember loginMember) {
        waitingService.deleteWaiting(id, loginMember);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/waitings/{id}")
    public ResponseEntity<Void> deleteWaitingByAdmin(@PathVariable Long id) {
        waitingService.deleteWaitingByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getAllWaitings() {
        List<WaitingResponse> waitingResponses = waitingService.getAllWaitings();
        return ResponseEntity.ok(waitingResponses);
    }
}
