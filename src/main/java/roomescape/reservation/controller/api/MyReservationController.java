package roomescape.reservation.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.reservation.service.WaitingFacadeService;
import roomescape.reservation.controller.dto.response.MyReservationAndWaitingResponse;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class MyReservationController {

    private final WaitingFacadeService waitingFacadeService;

    public MyReservationController(final WaitingFacadeService waitingFacadeService) {
        this.waitingFacadeService = waitingFacadeService;
    }

    @GetMapping("/mine")
    public List<MyReservationAndWaitingResponse> readMyReservations(@AuthMember Member member) {
        return waitingFacadeService.readMyReservations(member);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") long id) {
        waitingFacadeService.removeReservation(id);
    }
}
