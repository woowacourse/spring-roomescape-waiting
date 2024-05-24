package roomescape.reservation.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoggedInMember;
import roomescape.reservation.dto.MyReservationWaitingResponse;
import roomescape.reservation.service.MyReservationWaitingService;

@RestController
@RequestMapping("/my/reservaitons")
public class MyReservationController {
    private final MyReservationWaitingService service;

    public MyReservationController(MyReservationWaitingService service) {
        this.service = service;
    }

    @GetMapping
    public List<MyReservationWaitingResponse> findMyReservations(LoggedInMember member) {
        return service.findMyReservationsWaitings(member.id());
    }
}
