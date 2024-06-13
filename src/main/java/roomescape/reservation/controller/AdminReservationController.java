package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import roomescape.auth.principal.AuthenticatedMember;
import roomescape.reservation.dto.*;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.ThemeService;
import roomescape.reservation.service.WaitingService;
import roomescape.resolver.Authenticated;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final WaitingService waitingService;

    public AdminReservationController(
            final ReservationService reservationService,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final WaitingService waitingService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.waitingService = waitingService;
    }

    @GetMapping("/admin/reservations")
    public List<ReservationResponse> searchReservations(@ModelAttribute SearchReservationsRequest request) {
        return reservationService.searchReservations(request)
                .stream()
                .map(ReservationResponse::from)
                .toList();

    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@Valid @RequestBody final SaveReservationRequest request) {
        final Reservation savedReservation = reservationService.saveReservation(request);

        return ResponseEntity.created(URI.create("/reservations/" + savedReservation.getId()))
                .body(ReservationResponse.from(savedReservation));
    }

    @DeleteMapping("/admin/reservations/{reservation-id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservation-id") final Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeResponse> saveReservationTime(@RequestBody final SaveReservationTimeRequest request) {
        final ReservationTime savedReservationTime = reservationTimeService.saveReservationTime(request);

        return ResponseEntity.created(URI.create("/times/" + savedReservationTime.getId()))
                .body(ReservationTimeResponse.from(savedReservationTime));
    }

    @DeleteMapping("/admin/times/{reservation-time-id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("reservation-time-id") final Long reservationTimeId) {
        reservationTimeService.deleteReservationTime(reservationTimeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeResponse> saveTheme(@RequestBody final SaveThemeRequest request) {
        final Theme savedTheme = themeService.saveTheme(request);

        return ResponseEntity.created(URI.create("/themes/" + savedTheme.getId()))
                .body(ThemeResponse.from(savedTheme));
    }

    @DeleteMapping("/admin/themes/{theme-id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("theme-id") final Long themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/waitings")
    public List<WaitingResponse> getWaitings() {
        return waitingService.getWaitings()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    @DeleteMapping("/admin/waitings/{waiting-id}")
    public ResponseEntity<Void> deleteWaitings(@PathVariable("waiting-id") final Long waitingId,
                                               @Authenticated AuthenticatedMember authenticatedMember) {
        waitingService.deleteWaiting(waitingId, authenticatedMember);
        return ResponseEntity.noContent().build();
    }
}
