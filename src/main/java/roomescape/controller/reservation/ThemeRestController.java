package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.service.ThemeService;
import roomescape.service.dto.theme.PopularThemeRequest;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@RestController
public class ThemeRestController {

    private final ThemeService themeService;

    public ThemeRestController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public List<ThemeResponse> findAllThemes() {
        return themeService.findAllThemes();
    }

    @GetMapping("/themes/popular")
    public List<ThemeResponse> findTopBookedThemes(@RequestParam(name = "start-date") String startDate,
                                                   @RequestParam(name = "end-date") String endDate,
                                                   @RequestParam Integer count) {
        return themeService.findTopBookedThemes(new PopularThemeRequest(startDate, endDate, count));
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.createTheme(request);
        URI uri = UriComponentsBuilder.fromPath("/themes/{id}").build(response.getId());

        return ResponseEntity.created(uri).body(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/themes/{id}")
    public void deleteTheme(@PathVariable long id) {
        themeService.deleteTheme(id);
    }
}
