package roomescape.controller.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.AuthMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.entity.Member;
import roomescape.entity.WaitingStatus;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    private WaitingController(
        WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public List<WaitingResponse> readWaitings() {
        return waitingService.findAllWaitings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponse createWaiting(
        @AuthMember Member member,
        @RequestBody WaitingRequest request) {
        return waitingService.addWaitingAfterNow(member, request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWaiting(@PathVariable Long id, @RequestParam WaitingStatus status) {
        waitingService.updateWaitingAndReservationStatus(id, status);
    }
}
