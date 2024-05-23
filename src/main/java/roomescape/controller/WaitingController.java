package roomescape.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.LoginMember;
import roomescape.controller.helper.RoleAllowed;
import roomescape.domain.Member;
import roomescape.service.WaitingService;
import roomescape.service.dto.WaitingRequest;
import roomescape.service.dto.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @RoleAllowed
    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> saveWaiting(@RequestBody WaitingRequest request,
                                                       @LoginMember Member member) {
        WaitingResponse response = waitingService.save(request, member);
        return ResponseEntity.created(URI.create("/waitings/" + response.getId())).body(response);
    }

    @RoleAllowed
    @DeleteMapping("/waitings/{waitingId}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long waitingId) {
        waitingService.delete(waitingId);
        return ResponseEntity.noContent().build();
    }
}
