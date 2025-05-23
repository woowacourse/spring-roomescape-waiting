package roomescape.controller.api;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.waiting.MemberWaitingCreateRequest;
import roomescape.dto.waiting.WaitingCreateRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {


    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaiting(@CurrentMember LoginInfo loginInfo,
                                                      @RequestBody final MemberWaitingCreateRequest request) {
        WaitingCreateRequest waitingCreateRequest = new WaitingCreateRequest(request.date(),
                loginInfo.id(),
                request.themeId(),
                request.timeId());
        WaitingResponse response = waitingService.createWaiting(waitingCreateRequest);
        return ResponseEntity.created(URI.create("waitings/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting (@PathVariable("id") final Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
