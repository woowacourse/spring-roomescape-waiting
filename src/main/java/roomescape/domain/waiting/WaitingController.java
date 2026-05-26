package roomescape.domain.waiting;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

}
