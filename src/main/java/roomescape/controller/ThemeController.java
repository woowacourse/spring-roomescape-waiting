package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public List<ThemeResponse> findAll() {
        return themeService.findAll();
    }

    @GetMapping("/themes/ranking")
    public List<ThemeResponse> findAndOrderByPopularity(@RequestParam LocalDate start,
                                                        @RequestParam LocalDate end,
                                                        @RequestParam int count) {
        return themeService.findAndOrderByPopularity(start, end, count);
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeResponse> save(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse saved = themeService.save(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + saved.id()))
                .body(saved);
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
