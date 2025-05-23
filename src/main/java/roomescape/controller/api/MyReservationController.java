package roomescape.controller.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.annotation.AuthMember;
import roomescape.dto.response.MyReservationResponse;
import roomescape.entity.Member;
import roomescape.service.MyReservationService;

@RestController
@RequestMapping("/reservations/mine")
public class MyReservationController {

    private final MyReservationService myReservationService;

    private MyReservationController(MyReservationService myReservationService) {
        this.myReservationService = myReservationService;
    }

    @GetMapping
    public List<MyReservationResponse> readMyReservations(@AuthMember Member member) {
        return myReservationService.findReservationsAndWaitingsByMemberId(member);
    }
}
