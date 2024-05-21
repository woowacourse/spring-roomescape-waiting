package roomescape.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.auth.AuthenticationPrincipal;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.MemberResponse;
import roomescape.dto.auth.LoginMember;
import roomescape.dto.reservation.MemberReservationSaveRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationSaveRequest;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final MemberService memberService;
    private final WaitingService waitingService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingController(final MemberService memberService,
                             final WaitingService waitingService,
                             final ReservationTimeService reservationTimeService,
                             final ThemeService themeService) {
        this.memberService = memberService;
        this.waitingService = waitingService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createWaiting(@AuthenticationPrincipal final LoginMember loginMember,
                                                             @RequestBody final MemberReservationSaveRequest request) {
        final MemberResponse memberResponse = memberService.findById(loginMember.id());
        final ReservationSaveRequest saveRequest = request.generateReservationSaveRequest(memberResponse);

        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.findById(request.timeId());
        final ThemeResponse themeResponse = themeService.findById(request.themeId());

        final Reservation waiting = saveRequest.toWaiting(memberResponse, themeResponse, reservationTimeResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingService.create(waiting));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> cancelWaiting(@AuthenticationPrincipal final LoginMember loginMember,
                                                             @PathVariable Long reservationId) {
        ReservationResponse reservationResponse = waitingService.checkOwn(loginMember.id(), reservationId);
        waitingService.cancel(reservationResponse.id());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
