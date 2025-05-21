package roomescape.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.UserProfileResponse;
import roomescape.service.ReservationService;
import roomescape.service.UserService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final UserService memberService;
    private final ReservationService reservationService;

    public MemberController(UserService memberService, ReservationService reservationService) {
        this.memberService = memberService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<UserProfileResponse> findAllUser() {
        return memberService.findAllUserProfile();
    }
}
