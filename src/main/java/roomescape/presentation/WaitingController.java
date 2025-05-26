package roomescape.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.dto.request.WaitingCreateRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping(value = "/api/waiting")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createNewWaiting(
            @Authenticated Long memberId,
            @Valid @RequestBody WaitingCreateRequest request) {
        WaitingResponse waitingResponse = waitingService.createWaiting(
                memberId, request.timeId(), request.themeId(), request.date());
        return ResponseEntity
                .created(URI.create("/reservations/waitings/" + waitingResponse.id()))
                .body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
