package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.infrastructure.MemberId;
import roomescape.service.reservation.WaitingService;
import roomescape.service.reservation.dto.request.WaitingRequest;
import roomescape.service.reservation.dto.response.WaitingResponse;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> postWaiting(
            @RequestBody @Valid WaitingRequest waitingRequest,
            @MemberId Long memberId
    ) {
        WaitingResponse waitingResponse = waitingService.createWaiting(waitingRequest, memberId);

        URI location = UriComponentsBuilder.newInstance()
                .path("/waitings/{id}")
                .buildAndExpand(waitingResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent()
                .build();
    }
}
