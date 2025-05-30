package roomescape.controller;

import jakarta.validation.Valid;
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
        waitingService.deleteByMemberIdAndWaitingId(loginMemberInfo.id(), waitingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(@LoginMember LoginMemberInfo loginMemberInfo,
                                                         @Valid @RequestBody CreateWaitingRequest createWaitingRequest) {
        WaitingResult waitingresult = waitingService.create(createWaitingRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(WaitingResponse.from(waitingresult));
    }
}
