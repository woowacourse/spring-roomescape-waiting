package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingRequest;
import roomescape.controller.dto.WaitingResponse;
import roomescape.service.dto.WaitingWithNumber;
import roomescape.service.WaitingService;

@Validated
@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(@RequestBody @Valid WaitingRequest waitingRequest) {
        WaitingWithNumber waitingWithNumber = waitingService.saveWaiting(
                waitingRequest.name(),
                waitingRequest.date(),
                waitingRequest.timeId(),
                waitingRequest.themeId());

        return ResponseEntity.created(URI.create("/waitings/" + waitingWithNumber.waiting().getId()))
                .body(WaitingResponse.from(waitingWithNumber));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable
            long id,
            @RequestParam @NotBlank
            String userName) {
        waitingService.removeWaiting(id, userName);
        return ResponseEntity.noContent().build();
    }
}
