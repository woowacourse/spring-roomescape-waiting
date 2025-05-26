package roomescape.reservation.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.reservation.service.WaitingFacadeService;
import roomescape.reservation.controller.dto.response.MyReservationAndWaitingResponse;
import roomescape.waiting.controller.dto.request.WaitingRequest;
import roomescape.waiting.controller.dto.response.WaitingResponse;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class MyReservationController {

    private final WaitingFacadeService waitingFacadeService;

    public MyReservationController(final WaitingFacadeService waitingFacadeService) {
        this.waitingFacadeService = waitingFacadeService;
    }

    @PostMapping("/waiting")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponse createReservationWaiting(
            @AuthMember Member member,
            @RequestBody @Valid WaitingRequest request
    ) {
        return WaitingResponse.from(waitingFacadeService.addWaiting(member, request));
    }

    @GetMapping("/mine")
    public List<MyReservationAndWaitingResponse> readMyReservations(final @AuthMember Member member) {
        return waitingFacadeService.readMyReservations(member);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(final @PathVariable("id") long id) {
        waitingFacadeService.removeReservation(id);
    }
}
