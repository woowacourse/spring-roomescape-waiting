package roomescape.waiting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.waiting.controller.dto.WaitingAddRequest;
import roomescape.waiting.controller.dto.WaitingAddResponse;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.WaitingAddCommand;
import roomescape.waiting.service.dto.WaitingInfo;

@RestController
@RequestMapping("/waiting")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingAddResponse> addWaiting(
            final LoginMemberInfo loginMember,
            @RequestBody final WaitingAddRequest request
    ) {
        WaitingAddCommand command = request.toCommand(loginMember.id());
        WaitingInfo waitingInfo = waitingService.addWaiting(command);
        WaitingAddResponse response = new WaitingAddResponse(waitingInfo);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> cancel(
            final LoginMemberInfo loginMember,
            @PathVariable(value = "waitingId") final long waitingId
    ) {
        waitingService.cancelById(waitingId, loginMember);
        return ResponseEntity.noContent().build();
    }
}
