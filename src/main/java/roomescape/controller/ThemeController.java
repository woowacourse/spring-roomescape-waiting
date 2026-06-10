package roomescape.controller;

import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ThemeListResponse;
import roomescape.facade.ThemeFacade;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeFacade themeFacade;

    public ThemeController(ThemeFacade themeFacade) {
        this.themeFacade = themeFacade;
    }

    @GetMapping
    public ResponseEntity<ThemeListResponse> findAll() {
        ThemeListResponse response = themeFacade.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ranking")
    public ResponseEntity<ThemeListResponse> findRanking(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        ThemeListResponse response = themeFacade.findRanking(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
