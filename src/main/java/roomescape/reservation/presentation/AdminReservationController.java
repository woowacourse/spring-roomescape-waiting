package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.application.WaitingService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final WaitingService waitingService;

    public AdminReservationController(ReservationService reservationService, MemberService memberService,
                                      ReservationTimeService reservationTimeService, ThemeService themeService,
                                      WaitingService waitingService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid AdminReservationSaveRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        Member member = memberService.findById(request.memberId());
        Reservation newReservation = request.toModel(theme, reservationTime, member);
        Reservation createdReservation = reservationService.createReservation(newReservation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(createdReservation));
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> findWaitings() {
        List<Waiting> waitings = waitingService.findAll();
        return ResponseEntity.ok(waitings.stream()
                .map(WaitingResponse::from)
                .toList());
    }
}
