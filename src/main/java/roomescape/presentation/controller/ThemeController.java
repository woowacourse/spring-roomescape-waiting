package roomescape.presentation.controller;

import jakarta.validation.Valid;
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
import roomescape.application.ThemeApplicationService;
import roomescape.domain.Theme;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;
import roomescape.presentation.dto.ThemeResponses;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeApplicationService themeApplicationService;

    public ThemeController(
            ThemeApplicationService themeApplicationService
    ) {
        this.themeApplicationService = themeApplicationService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> add(
            @RequestBody @Valid ThemeRequest request
    ) {
        Theme savedTheme = themeApplicationService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ThemeResponse.from(savedTheme));
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> search() {
        List<Theme> themes = themeApplicationService.findAll();

        return ResponseEntity.ok()
                .body(ThemeResponses.from(themes));
    }

    @GetMapping("/popular")
    public ResponseEntity<ThemeResponses> searchPopular(
            @RequestParam(required = false) LocalDate now,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        LocalDate baseDate = (now != null) ? now : LocalDate.now();
        List<Theme> popularThemes = themeApplicationService.findPopular(baseDate, days, limit);

        return ResponseEntity.ok()
                .body(ThemeResponses.from(popularThemes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeApplicationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
