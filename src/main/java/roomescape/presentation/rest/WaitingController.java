package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.domain.user.User;
import roomescape.domain.waiting.Waiting;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.CreateWaitingRequest;
import roomescape.presentation.response.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    @ResponseStatus(CREATED)
    public WaitingResponse createWaiting(
            @Authenticated final User user, @RequestBody @Valid final CreateWaitingRequest request
    ) {
        Waiting waiting = waitingService.saveWaiting(user, request.date(), request.timeId(), request.themeId());

        return WaitingResponse.fromWaiting(waiting);
    }

    @GetMapping("/admin/waitings")
    public List<WaitingResponse> readAllWaitings() {
        List<Waiting> waitings = waitingService.findAllWaitings();

        return WaitingResponse.fromWaitings(waitings);
    }

    @DeleteMapping("/waitings/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteWaitingById(
            @PathVariable("id") final long id
    ) {
        waitingService.removeById(id);
    }
}
