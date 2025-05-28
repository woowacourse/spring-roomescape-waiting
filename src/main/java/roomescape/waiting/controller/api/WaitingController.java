package roomescape.waiting.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.waiting.controller.dto.request.WaitingRequest;
import roomescape.waiting.controller.dto.response.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }


    @DeleteMapping("/waiting/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWaiting(final @PathVariable("id") long id) {
        waitingService.removeWaiting(id);
    }

}
