package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.jwt.TokenProvider;
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
    public ResponseEntity<List<UserReservationResponse>> getMemberReservations(
            @CookieValue(name = "token", required = false) String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        List<UserReservationResponse> allMemberReservations = userReservationService.findAllMemberReservations(
                memberId);
        return ResponseEntity.ok(allMemberReservations);
    }
}
