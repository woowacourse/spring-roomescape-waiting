package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingRequest;
import roomescape.service.SessionService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final SessionService sessionService;

    public WaitingController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<Void> apply(@RequestBody @Valid WaitingRequest request) {
        sessionService.addWaiting(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable long id,
            @RequestParam @NotBlank String userName) {
        sessionService.cancelWaiting(id, userName);
        return ResponseEntity.noContent().build();
    }
}
