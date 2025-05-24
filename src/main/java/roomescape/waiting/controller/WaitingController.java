package roomescape.waiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.infrastructure.methodargument.AuthorizedMember;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.WaitingCreateResponse;
import roomescape.waiting.service.WaitingServiceFacade;

@RestController
@RequestMapping("/waiting")
public class WaitingController {
    private final WaitingServiceFacade waitingService;

    public WaitingController(WaitingServiceFacade waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingCreateResponse> create(
            @RequestBody @Valid WaitingCreateRequest waitingCreateRequest,
            @AuthorizedMember MemberPrincipal memberPrincipal
    ) {
        WaitingCreateResponse response = waitingService.createWaiting(waitingCreateRequest, memberPrincipal);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
