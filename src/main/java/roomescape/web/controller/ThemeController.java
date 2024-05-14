package roomescape.web.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.domain.policy.WeeklyRankingPolicy;
import roomescape.service.ThemeService;
import roomescape.web.dto.request.theme.ThemeRequest;
import roomescape.web.dto.response.theme.ThemeResponse;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAllTheme() {
        List<ThemeResponse> response = themeService.findAllTheme();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ThemeResponse>> findAllPopularTheme() {
        List<ThemeResponse> response = themeService.findAllPopularTheme(new WeeklyRankingPolicy());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> saveTheme(@Valid @RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.saveTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + response.id())).body(response);
    }

    @DeleteMapping("/{theme_id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable(value = "theme_id") Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
