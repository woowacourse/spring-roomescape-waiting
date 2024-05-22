package roomescape.core.controller;

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
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.waiting.MemberWaitingRequest;
import roomescape.core.dto.waiting.WaitingRequest;
import roomescape.core.dto.waiting.WaitingResponse;
import roomescape.core.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(@Valid @RequestBody final MemberWaitingRequest request,
                                                  final LoginMember loginMember) {
        final WaitingRequest waitingRequest = new WaitingRequest(loginMember.getId(), request.getDate(),
                request.getTimeId(),
                request.getThemeId());

        final WaitingResponse response = waitingService.create(waitingRequest);
        return ResponseEntity.created(URI.create("/waitings/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findAll() {
        return ResponseEntity.ok(waitingService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        waitingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
