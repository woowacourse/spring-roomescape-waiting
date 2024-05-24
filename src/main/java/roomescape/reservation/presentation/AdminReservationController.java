package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.application.WaitingReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final WaitingReservationService waitingReservationService;

    public AdminReservationController(ReservationService reservationService, MemberService memberService,
                                      ReservationTimeService reservationTimeService, ThemeService themeService,
                                      WaitingReservationService waitingReservationService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.waitingReservationService = waitingReservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid AdminReservationSaveRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        Member member = memberService.findById(request.memberId());
        Reservation newReservation = request.toModel(theme, reservationTime, member);
        Reservation createdReservation = reservationService.create(newReservation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(createdReservation));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        List<Reservation> reservations = reservationService.findReservations();
        return ResponseEntity.ok(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    @GetMapping("/searching")
    public ResponseEntity<List<ReservationResponse>> findReservationsByMemberIdAndThemeIdAndDateBetween(
            @RequestParam Long memberId, @RequestParam Long themeId,
            @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate) {
        List<Reservation> reservations = reservationService.findReservationsByMemberIdAndThemeIdAndDateBetween(
                memberId, themeId, fromDate, toDate);
        return ResponseEntity.ok(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findWaitingReservations() {
        List<Reservation> waitingReservations = waitingReservationService.findWaitingReservations();
        return ResponseEntity.ok(waitingReservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable Long id, Member loginAdminMember) {
        waitingReservationService.deleteWaitingReservation(id, loginAdminMember);
        return ResponseEntity.noContent().build();
    }
}
