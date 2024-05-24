package roomescape.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.auth.AuthenticationPrincipal;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.MemberResponse;
import roomescape.dto.auth.LoginMember;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationSaveRequest;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitingService;

@RequestMapping("/waitings")
@RestController
public class WaitingController {

    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final WaitingService waitingService;

    public WaitingController(
            final MemberService memberService,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final WaitingService waitingService)
    {
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservationWaiting(
            @AuthenticationPrincipal final LoginMember loginMember,
            @RequestBody final ReservationSaveRequest request) {
        final MemberResponse memberResponse = memberService.findById(loginMember.id());
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.findById(request.timeId());
        final ThemeResponse themeResponse = themeService.findById(request.themeId());

        final Reservation reservation = request.toModel(memberResponse, themeResponse, reservationTimeResponse, request.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingService.createReservationWaiting(reservation));
    }
}
