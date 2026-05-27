package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ControllerThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.dto.response.ServiceThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ControllerThemeResponse>> findAll() {
        List<ServiceThemeResponse> serviceResponses = themeService.findAll();
        List<ControllerThemeResponse> controllerResponses = serviceResponses.stream()
                .map(ControllerThemeResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponses);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ControllerThemeResponse>> findRanking(
            @RequestParam("start-date") LocalDate startDate,
            @RequestParam("end-date") LocalDate endDate
    ) {
        List<ServiceThemeResponse> serviceResponses = themeService.findRanking(startDate, endDate);
        List<ControllerThemeResponse> controllerResponses = serviceResponses.stream()
                .map(ControllerThemeResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponses);
    }
}
