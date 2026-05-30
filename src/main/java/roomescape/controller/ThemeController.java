package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.facade.ThemeFacade;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeFacade themeFacade;

    public ThemeController(ThemeFacade themeFacade) {
        this.themeFacade = themeFacade;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        return ResponseEntity.ok(themeFacade.findAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ThemeResponse>> findRanking(
            @RequestParam("start-date") LocalDate startDate,
            @RequestParam("end-date") LocalDate endDate
    ) {
        return ResponseEntity.ok(themeFacade.findRanking(startDate, endDate));
    }
}
