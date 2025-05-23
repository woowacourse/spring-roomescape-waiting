package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.reservation.controller.dto.WaitingAddRequest;
import roomescape.reservation.controller.dto.WaitingAddResponse;
import roomescape.reservation.service.WaitingService;
import roomescape.reservation.service.dto.WaitingAddCommand;
import roomescape.reservation.service.dto.WaitingInfo;

@RestController
@RequestMapping("/waiting")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingAddResponse> addWaiting(LoginMemberInfo loginMember, @RequestBody WaitingAddRequest request) {
        WaitingAddCommand command = request.toCommand(loginMember.id());
        WaitingInfo waitingInfo = waitingService.addWaiting(command);
        WaitingAddResponse response = new WaitingAddResponse(waitingInfo);
        return ResponseEntity.ok().body(response);
    }
}
