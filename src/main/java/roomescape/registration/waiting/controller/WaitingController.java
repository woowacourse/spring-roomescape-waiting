package roomescape.registration.waiting.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.registration.waiting.dto.WaitingRequest;
import roomescape.registration.waiting.dto.WaitingResponse;
import roomescape.registration.waiting.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    // 예약 대기 버튼을 누르면 이 메서드가 실행된다.
    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> createWaiting(@RequestBody WaitingRequest waitingRequest,
                                                         @LoginMemberId long memberId) {
        WaitingResponse waiting = waitingService.addWaiting(waitingRequest, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(waiting);
    }

    // 해당 uri로 검색하면 이 메서드가 실행된다
    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> waitingList() {
        List<WaitingResponse> waitings = waitingService.findWaitings();

        return ResponseEntity.ok(waitings);
    }
}
