package roomescape.waiting.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.controller.response.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RequiredArgsConstructor
@RestController
public class WaitingAdminApiController {

    private final WaitingService waitingService;

    @GetMapping("/waiting")
    public ResponseEntity<List<WaitingResponse>> getWaitingReservations() {
        List<WaitingResponse> responses = waitingService.getWaitings();

        return ResponseEntity.ok(responses);
    }
}
