package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.controller.request.CreateWaitingRequest;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.WaitingResponse;
import roomescape.service.WaitingService;
import roomescape.service.result.WaitingResult;

import java.util.List;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findWaitings() {
        List<WaitingResult> waitingResults = waitingService.findAll();
        return ResponseEntity.ok(WaitingResponse.from(waitingResults));
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> deleteWaiting(@LoginMember LoginMemberInfo loginMemberInfo, @PathVariable Long waitingId) {
        waitingService.delete(loginMemberInfo.id(), waitingId); //TODO: Service로 넘기는 포맷 고민. memberId와 waitingId를 그대로 넘기기 or DTO
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(@LoginMember LoginMemberInfo loginMemberInfo,
                                                         @RequestBody CreateWaitingRequest createWaitingRequest) {
        WaitingResult waitingresult = waitingService.create(createWaitingRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(WaitingResponse.from(waitingresult));
    }

    @PostMapping("/{waitingId}/approve")
    public ResponseEntity<Void> approveWaiting(@PathVariable Long waitingId) {
        waitingService.approve(waitingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{waitingId}/reject")
    public ResponseEntity<Void> rejectWaiting(@PathVariable Long waitingId) {
        waitingService.deleteById(waitingId);
        return ResponseEntity.noContent().build();
    }
}
