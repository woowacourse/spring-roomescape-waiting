package roomescape.controller.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.dto.SessionMember;
import roomescape.service.WaitingService;
import roomescape.service.request.WaitingCreateRequest;
import roomescape.service.response.WaitingResponse;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(
            @RequestBody @Valid final WaitingCreateRequest request,
            final SessionMember sessionMember
    ) {
        final WaitingResponse response = waitingService.createWaiting(request, sessionMember.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable final Long id,
            final SessionMember sessionMember) {
        waitingService.deleteWaitingById(id, sessionMember.id());
        return ResponseEntity.noContent().build();
    }

}
