package roomescape.theme.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final ReservationService reservationService;

    public ThemeController(ThemeService themeService, ReservationService reservationService) {
        this.themeService = themeService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        System.out.println("====== getAllThemes EXECUTED ======");
        List<ThemeResponse> responses = themeService.findAll()
                .stream()
                .map(ThemeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping(params = "popular=true")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam("period") int period,
            @RequestParam("limit") int limit
    ) {
        System.out.println("====== getPopularThemes EXECUTED, period: " + period + ", limit: " + limit + " ======");
        if (period < 1 || limit < 1) {
            System.out.println("====== THROWING EXCEPTION ======");
            throw new InvalidRequestValueException();
        }

        List<ThemeResponse> responses = reservationService.findPopularThemes(period, limit).popularThemes()
                .stream()
                .map(ThemeResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
