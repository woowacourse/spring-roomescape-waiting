package roomescape.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.dto.AvailableTimeResponse;
import roomescape.controller.dto.ThemeResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@RequestMapping("/themes")
@RestController
@Validated
public class UserThemeController {

    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public UserThemeController(ThemeService themeService, ReservationTimeService reservationTimeService) {
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResponse> responses = themeService.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> findPopularThemes() {
        List<ThemeResponse> responses = themeService.findPopularThemes().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/available-times")
    public ResponseEntity<List<AvailableTimeResponse>> findAvailableTimes(
            @PathVariable long id,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AvailableTimeResponse> responses = reservationTimeService.findAvailableTimes(id, date).stream()
                .map(AvailableTimeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
