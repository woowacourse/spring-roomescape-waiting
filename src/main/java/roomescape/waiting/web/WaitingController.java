package roomescape.waiting.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.auth.LoginMember;
import roomescape.auth.service.WaitingAuthorizationService;
import roomescape.member.Member;
import roomescape.waiting.Waiting;
import roomescape.waiting.web.WaitingRequestDto;
import roomescape.waiting.web.WaitingResponse;
import roomescape.waiting.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;
    private final WaitingAuthorizationService waitingAuthorizationService;

    public WaitingController(WaitingService waitingService, WaitingAuthorizationService waitingAuthorizationService) {
        this.waitingService = waitingService;
        this.waitingAuthorizationService = waitingAuthorizationService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @LoginMember Member member,
            @Valid @RequestBody WaitingRequestDto request) {

        Waiting waiting = waitingService.create(request, member);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(waiting.getId())
                .toUri();
        return ResponseEntity.created(uri).body(WaitingResponse.from(waiting));
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAllByMember(@LoginMember Member member) {
        List<WaitingResponse> responses = waitingService.findAllByMemberId(member.getId()).stream()
                .map(WaitingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @LoginMember Member member,
            @PathVariable Long id
    ) {
        waitingAuthorizationService.validateMemberCanAccess(member, id);
        waitingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
