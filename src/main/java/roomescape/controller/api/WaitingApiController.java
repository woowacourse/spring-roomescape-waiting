package roomescape.controller.api;

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
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.controller.api.dto.request.WaitingRequest;
import roomescape.controller.api.dto.response.WaitingResponse;
import roomescape.controller.api.dto.response.WaitingsResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.output.WaitingOutput;

@RestController
@RequestMapping("/waitings")
public class WaitingApiController {

    private final WaitingService waitingService;

    public WaitingApiController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(
            @RequestBody final WaitingRequest waitingRequest,
            final LoginMemberRequest loginMemberRequest) {
        final WaitingOutput output = waitingService.createWaiting(waitingRequest.toInput(loginMemberRequest.id()));
        return ResponseEntity.created(URI.create("/waitings/" + output.id()))
                .body(WaitingResponse.toResponse(output));
    }

    @GetMapping
    public ResponseEntity<WaitingsResponse> getAllWaitings() {
        final List<WaitingOutput> outputs = waitingService.getAllWaitings();
        return ResponseEntity.ok(WaitingsResponse.toResponse(outputs));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable final long id,
            final LoginMemberRequest loginMemberRequest) {
        waitingService.deleteWaiting(id, loginMemberRequest);
        return ResponseEntity.noContent()
                .build();
    }
}
