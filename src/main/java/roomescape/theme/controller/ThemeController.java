package roomescape.theme.controller;

import java.time.LocalDate;
import java.util.List;
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
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ReservedThemeResponse;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeRankingService;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final ThemeRankingService themeRankingService;

    public ThemeController(ThemeService themeService, ThemeRankingService themeRankingService) {
        this.themeService = themeService;
        this.themeRankingService = themeRankingService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResponse> responses = themeService.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(
            @RequestBody ThemeCreateRequest themeCreateRequest
    ) {
        ThemeResponse response = themeService.create(themeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id
    ) {
        themeService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/most-reserved-themes")
    public ResponseEntity<List<ReservedThemeResponse>> findMostReserved(
            @RequestParam int limit,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<ReservedThemeResponse> responses = themeRankingService.findMostReserved(limit, startDate, endDate);

        return ResponseEntity.ok(responses);
    }
}
