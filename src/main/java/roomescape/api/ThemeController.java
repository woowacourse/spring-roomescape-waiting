package roomescape.api;

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
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.dto.ThemeResponses;
import roomescape.facade.ReservationFacade;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final ReservationFacade reservationFacade;

    public ThemeController(
            ThemeService themeService,
            ReservationFacade reservationFacade
    ) {
        this.themeService = themeService;
        this.reservationFacade = reservationFacade;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> add(
            @RequestBody @Valid ThemeRequest request
    ) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnailImageUrl());
        Theme savedTheme = themeService.addTheme(theme);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ThemeResponse.from(savedTheme));
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> search() {
        List<Theme> themes = themeService.getThemes();

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
        List<Theme> popularThemes = themeService.getPopularThemes(baseDate, days, limit);

        return ResponseEntity.ok()
                .body(ThemeResponses.from(popularThemes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationFacade.deleteTheme(id);

        return ResponseEntity.noContent().build();
    }
}
