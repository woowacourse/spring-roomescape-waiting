package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.UserReservationResponse;
import roomescape.reservation.service.UserReservationService;

@RestController
@RequestMapping
public class UserReservationController {

    private final UserReservationService userReservationService;
    private final TokenProvider tokenProvider;

    public UserReservationController(UserReservationService userReservationService, TokenProvider tokenProvider) {
        this.userReservationService = userReservationService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/member/reservations")
    public ResponseEntity<List<UserReservationResponse>> getMemberReservations(Member member) {
        List<UserReservationResponse> response = userReservationService.findAllMemberReservations(member);
        return ResponseEntity.ok(response);
    }
}
