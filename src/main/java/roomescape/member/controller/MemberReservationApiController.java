package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.AuthenticationPrincipal;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.service.MyReservationService;

@Controller
@RequestMapping("/reservations-mine")
public class MemberReservationApiController {
    private final MyReservationService myReservationService;

    public MemberReservationApiController(MyReservationService myReservationService) {
        this.myReservationService = myReservationService;
    }

    @GetMapping
    public ResponseEntity<List<MyReservationResponse>> findAllByMemberId(
            @AuthenticationPrincipal MemberResponse memberResponse
    ) {
        return ResponseEntity.ok(myReservationService.findAllMyReservationByMember(memberResponse.id()));
    }
}
