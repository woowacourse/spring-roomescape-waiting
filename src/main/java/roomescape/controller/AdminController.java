package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.request.ReservationTimeRequest;
import roomescape.controller.dto.request.ThemeRequest;
import roomescape.controller.dto.response.ReservationResponses;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public AdminController(ReservationService reservationService, ThemeService themeService, ReservationTimeService reservationTimeService) {
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<ReservationResponses> findReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Reservation> reservations = reservationService.findReservations(page, size);
        ReservationResponses response = ReservationResponses.from(reservations);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest requestTheme) {
        Theme theme = themeService.createTheme(requestTheme.name(), requestTheme.description(), requestTheme.thumbnail());
        ThemeResponse response = ThemeResponse.from(theme);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> createTime(@Valid @RequestBody ReservationTimeRequest request) {
        ReservationTime time = reservationTimeService.createTime(request.startAt());
        ReservationTimeResponse response = ReservationTimeResponse.from(time);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteTime(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
