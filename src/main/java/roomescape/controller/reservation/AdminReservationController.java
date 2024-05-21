package roomescape.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.MemberResponse;
import roomescape.dto.reservation.AdminReservationSaveRequest;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

import java.util.List;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public AdminReservationController(
            final MemberService memberService,
            final ReservationService reservationService,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService)
    {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final AdminReservationSaveRequest request) {
        final MemberResponse memberResponse = memberService.findById(request.memberId());
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.findById(request.timeId());
        final ThemeResponse themeResponse = themeService.findById(request.themeId());

        final Reservation reservation = request.toModel(memberResponse, themeResponse, reservationTimeResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(reservation));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations (
            @ModelAttribute final ReservationFilterParam reservationFilterParam) {
        return ResponseEntity.ok(reservationService.findAllBy(reservationFilterParam));
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findReservationWaitings() {
        return ResponseEntity.ok(reservationService.findReservationWaitings());
    }
}
