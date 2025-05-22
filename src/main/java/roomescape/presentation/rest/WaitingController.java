package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.domain.user.User;
import roomescape.domain.waiting.Waiting;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.CreateWaitingRequest;
import roomescape.presentation.response.WaitingResponse;

@RestController
@RequestMapping("waitings")
public class WaitingController {

    private final WaitingService service;

    public WaitingController(final WaitingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public WaitingResponse createWaiting(
            @Authenticated final User user, @RequestBody @Valid final CreateWaitingRequest request
    ) {

        System.out.println(request);
        Waiting waiting = service.saveWaiting(user, request.date(), request.timeId(), request.themeId());
        return WaitingResponse.from(waiting);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteReservationById(@PathVariable("id") final long id) {
        service.removeById(id);
    }
}
