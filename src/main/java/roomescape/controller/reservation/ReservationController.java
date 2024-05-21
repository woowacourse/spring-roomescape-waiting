package roomescape.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.MyReservationsResponse;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationSaveRequest;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitingService;

import java.util.List;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationController(final MemberService memberService,
                                 final ReservationService reservationService,
                                 final WaitingService waitingService,
                                 final ReservationTimeService reservationTimeService,
                                 final ThemeService themeService) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@AuthenticationPrincipal final LoginMember loginMember,
                                                                 @RequestBody final MemberReservationSaveRequest request) {
        final MemberResponse memberResponse = memberService.findById(loginMember.id());
        final ReservationSaveRequest saveRequest = request.generateReservationSaveRequest(memberResponse);

        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.findById(request.timeId());
        final ThemeResponse themeResponse = themeService.findById(request.themeId());

        final Reservation reservation = saveRequest.toReservation(memberResponse, themeResponse, reservationTimeResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(reservation));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable final Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(@AuthenticationPrincipal final LoginMember loginMember) {
        final List<MyReservationResponse> myReservations = reservationService.findMyReservations(loginMember.id());
        final List<MyReservationResponse> myWaitings = waitingService.findMyWaitings(loginMember.id());
        final List<MyReservationResponse> responses = MyReservationsResponse.combine(myReservations, myWaitings);
        return ResponseEntity.ok(responses);
    }
}
