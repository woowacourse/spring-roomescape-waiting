package roomescape.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingRequest;
import roomescape.service.WaitingService;
import roomescape.service.dto.WaitingCommand;

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

        return ResponseEntity.ok(waitingService.waitingNumber(new WaitingCommand(name, date, timeId, themeId)));
    }

    @PostMapping
    public ResponseEntity<Void> apply(@RequestBody WaitingRequest waitingRequest) {
        waitingService.saveWaiting(waitingRequest.toCommand());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> cancel(@RequestParam @NotBlank String name,
                                       @RequestParam @NotNull LocalDate date,
                                       @RequestParam @NotNull Long timeId,
                                       @RequestParam @NotNull Long themeId) {

        waitingService.removeWaiting(new WaitingCommand(name, date, timeId, themeId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = {"date", "timeId", "themeId"})
    ResponseEntity<Integer> allWaiting(@RequestParam @NotNull LocalDate date,
                                       @RequestParam @NotNull Long timeId,
                                       @RequestParam @NotNull Long themeId) {
        return ResponseEntity.ok(waitingService.allWaiting(WaitingCommand.withoutName(date, timeId, themeId)));
    }
}
