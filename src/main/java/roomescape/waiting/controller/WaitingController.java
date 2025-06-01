package roomescape.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingCreateResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingCreateResponse> create(@Valid @RequestBody WaitingCreateRequest waitingCreateRequest,
                                                        Member member) {
        WaitingCreateResponse waitingCreateResponse = waitingService.create(waitingCreateRequest, member);
        return ResponseEntity.ok(waitingCreateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long waitingId) {
        waitingService.delete(waitingId);
        return ResponseEntity.noContent().build();
    }
}
