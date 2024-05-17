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
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.dto.response.ReservationTimeResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService, ThemeService themeService) {
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @RequestBody @Valid ReservationTimeSaveRequest request) {
        ReservationTime newReservationTime = request.toModel();
        ReservationTime createReservationTime = reservationTimeService.create(newReservationTime);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationTimeResponse.from(createReservationTime));
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();
        return ResponseEntity.ok(reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllByDateAndThemeId(
            @RequestParam LocalDate date, @RequestParam Long themeId) {
        Theme theme = themeService.findById(themeId);
        return ResponseEntity.ok(reservationTimeService.findAvailableReservationTimes(date, theme));
    }
}
