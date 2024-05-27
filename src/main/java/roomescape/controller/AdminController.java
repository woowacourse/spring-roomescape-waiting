package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationReadService;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationResponse;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ReservationCreateService reservationCreateService;
    private final ReservationReadService reservationReadService;
    private final ThemeService themeService;

    public AdminController(ReservationCreateService reservationCreateService, ReservationReadService reservationReadService, ThemeService themeService) {
        this.reservationCreateService = reservationCreateService;
        this.reservationReadService = reservationReadService;
        this.themeService = themeService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid AdminReservationRequest adminReservationRequest) {
        ReservationResponse reservationResponse = reservationCreateService.createAdminReservation(adminReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations/search")
    public List<ReservationResponse> findReservations(
            @ModelAttribute("ReservationFindRequest") ReservationFilterRequest reservationFilterRequest) {
        return reservationReadService.findByCondition(reservationFilterRequest);
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid ThemeRequest themeRequest, @LoginMemberId long memberId) {
        ThemeResponse themeResponse = themeService.create(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id())).body(themeResponse);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") long themeId) {
        themeService.deleteById(themeId);
        return ResponseEntity.noContent().build();
    }
}
