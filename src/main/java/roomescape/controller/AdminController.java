package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.dto.MemberResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public AdminController(
            MemberService memberService,
            ReservationService reservationService,
            ThemeService themeService,
            ReservationTimeService reservationTimeService
    ) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers() {
        List<MemberResponse> members = memberService.findAllMembers();
        return ResponseEntity.ok(members);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        ReservationResponse reservation = reservationService.save(request);
        URI uri = URI.create("/reservations/" + reservation.id());
        return ResponseEntity.created(uri).body(reservation);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok().body(reservations);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> searchReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ReservationResponse> reservations = reservationService.searchReservations(themeId, memberId, dateFrom,
                dateTo);
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody ThemeRequest request) {
        ThemeResponse theme = themeService.addTheme(request);
        URI location = URI.create("/themes/" + theme.id());
        return ResponseEntity.created(location).body(theme);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> addTime(@RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse time = reservationTimeService.addReservationTime(request);
        URI location = URI.create("/times/" + time.id());
        return ResponseEntity.created(location).body(time);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable("id") Long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }
}
