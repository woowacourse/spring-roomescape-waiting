package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.dto.SaveReservationRequest;
import roomescape.reservation.dto.SaveReservationTimeRequest;
import roomescape.reservation.dto.SaveThemeRequest;
import roomescape.reservation.dto.SearchReservationsRequest;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.dto.ReservationDto;
import roomescape.reservation.dto.ReservationTimeDto;
import roomescape.reservation.dto.ThemeDto;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.ThemeService;

import java.net.URI;
import java.util.List;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public AdminReservationController(
            final ReservationService reservationService,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @GetMapping("/admin/reservations")
    public List<ReservationResponse> searchReservations(@ModelAttribute SearchReservationsRequest request) {
        return reservationService.searchReservations(request)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@RequestBody final SaveReservationRequest request) {
        final ReservationDto savedReservation = reservationService.saveReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + savedReservation.id()))
                .body(ReservationResponse.from(savedReservation));
    }

    @DeleteMapping("/admin/reservations/{reservation-id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservation-id") final Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeResponse> saveReservationTime(@RequestBody final SaveReservationTimeRequest request) {
        final ReservationTimeDto savedReservationTime = reservationTimeService.saveReservationTime(request);

        return ResponseEntity.created(URI.create("/times/" + savedReservationTime.id()))
                .body(ReservationTimeResponse.from(savedReservationTime));
    }

    @DeleteMapping("/admin/times/{reservation-time-id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("reservation-time-id") final Long reservationTimeId) {
        reservationTimeService.deleteReservationTime(reservationTimeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeResponse> saveTheme(@RequestBody final SaveThemeRequest request) {
        final ThemeDto savedTheme = themeService.saveTheme(request);

        return ResponseEntity.created(URI.create("/themes/" + savedTheme.id()))
                .body(ThemeResponse.from(savedTheme));
    }

    @DeleteMapping("/admin/themes/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("themeId") final Long themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }
}
