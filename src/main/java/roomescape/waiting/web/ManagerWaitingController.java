package roomescape.waiting.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.auth.service.WaitingAuthorizationService;
import roomescape.member.Member;
import roomescape.waiting.web.WaitingResponse;
import roomescape.waiting.WaitingService;

@RestController
@RequestMapping("/manager/waitings")
public class ManagerWaitingController {

    private final WaitingService waitingService;
    private final WaitingAuthorizationService waitingAuthorizationService;

    public ManagerWaitingController(
            WaitingService waitingService,
            WaitingAuthorizationService waitingAuthorizationService
    ) {
        this.waitingService = waitingService;
        this.waitingAuthorizationService = waitingAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll(@LoginMember Member manager) {
        List<WaitingResponse> responses = waitingService.findAllByStoreId(manager.getStoreId()).stream()
                .map(WaitingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @LoginMember Member manager) {
        waitingAuthorizationService.validateManagerCanAccess(manager, id);
        waitingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
