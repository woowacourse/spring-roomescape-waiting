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
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, ReservationTimeService reservationTimeService,
                                 ThemeService themeService, MemberService memberService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationSaveRequest request,
                                                                 Member loginMember) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        Reservation newReservation = request.toModel(theme, reservationTime, loginMember, ReservationStatus.BOOKING);
        Reservation createReservation = reservationService.create(newReservation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(createReservation));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        List<Reservation> reservations = reservationService.findAll();
        return ResponseEntity.ok(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    @GetMapping("/searching")
    public ResponseEntity<List<ReservationResponse>> findReservationsByMemberIdAndThemeIdAndDateBetween(
            @RequestParam Long memberId, @RequestParam Long themeId,
            @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate) {
        Member member = memberService.findById(memberId);
        Theme theme = themeService.findById(themeId);
        List<Reservation> reservations = reservationService.findAllByMemberAndThemeAndDateBetween(
                member, theme, fromDate, toDate);
        return ResponseEntity.ok(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(Member loginMember) {
        List<Reservation> reservations = reservationService.findAllByMember(loginMember);
        return ResponseEntity.ok(reservations.stream()
                .map(reservation -> MyReservationResponse.of(reservation, waitingService.findLankByReservation(reservation)))
                .toList());
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> createWaitingReservation(@RequestBody @Valid ReservationSaveRequest request,
                                                                        Member loginMember) {
        final ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        final Theme theme = themeService.findById(request.themeId());
        final Reservation newReservation = request.toModel(theme, reservationTime, loginMember, ReservationStatus.WAITING);
        final Reservation createReservation = reservationService.createWaitingReservation(newReservation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(createReservation));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable("id") Long id) {
        reservationService.deleteWaitingReservation(id);
        return ResponseEntity.noContent().build();
    }
}
