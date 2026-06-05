package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingRequest;
import roomescape.controller.dto.WaitingResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.WaitingResult;

@RestController
@RequestMapping("/user/waitings")
public class UserWaitingController {

    private final WaitingService waitingService;

    public UserWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponse create(@RequestBody @Valid WaitingRequest request) {
        WaitingResult saved = waitingService.create(request.toCommand());
        return WaitingResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @RequestParam String name) {
        waitingService.cancelByOwner(id, name);
    }
}
