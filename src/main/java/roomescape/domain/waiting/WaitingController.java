package roomescape.domain.waiting;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
        @RequestBody @Valid WaitingRequest waitingRequest
    ) {

        WaitingResponse response = waitingService.createWaiting(waitingRequest);
        URI location = URI.create("/reservations/waiting/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteReservation(
        @PathVariable Long id
    ) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

}
