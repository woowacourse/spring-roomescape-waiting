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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.service.ThemeService;

//Todo 일부 기능 관리자만 접근할 수 있도록 깔끔하게 하는 방법 생각해보기
@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public List<ThemeResponse> findAll() {
        return themeService.findAll();
    }

    @GetMapping("/ranking")
    public List<ThemeResponse> findAndOrderByPopularity(@RequestParam LocalDate start,
                                                        @RequestParam LocalDate end,
                                                        @RequestParam int count) {
        return themeService.findAndOrderByPopularity(start, end, count);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> save(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse saved = themeService.save(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + saved.id()))
                .body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
