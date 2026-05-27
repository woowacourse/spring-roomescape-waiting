package roomescape.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Waiting;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<Integer> waitingNumber(@RequestParam @NotBlank String name,
                                                 @RequestParam @NotNull LocalDate date,
                                                 @RequestParam @NotNull Long timeId,
                                                 @RequestParam @NotNull Long themeId) {
        Waiting waiting = Waiting.transientOf(name, date, timeId, themeId);
        return ResponseEntity.ok(waitingService.waitingNumber(waiting));
    }

    @PostMapping
    public ResponseEntity<Void> apply(@RequestBody WaitingRequest waitingRequest) {
        Waiting waiting = Waiting.transientOf(waitingRequest.name(), waitingRequest.date(), waitingRequest.timeId(),
                waitingRequest.themeId());
        waitingService.saveWaiting(waiting);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable
            long id,
            @RequestParam @NotBlank
            String userName) {
        waitingService.removeWaiting(id, userName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = {"date", "timeId", "themeId"})
    ResponseEntity<Integer> allWaiting(@RequestParam @NotNull LocalDate date,
                                       @RequestParam @NotNull Long timeId,
                                       @RequestParam @NotNull Long themeId) {
        return ResponseEntity.ok(waitingService.allWaitingOf(date, timeId, themeId));
    }
}
