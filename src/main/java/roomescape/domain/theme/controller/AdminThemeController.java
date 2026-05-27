package roomescape.domain.theme.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.theme.dto.request.ThemeCreateRequestDto;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.theme.service.ThemeService;

@RestController
@RequestMapping("/api/admin/themes")
@Validated
public class AdminThemeController {

    private final ThemeService themeService;
    private final ThemeMapper themeMapper;

    public AdminThemeController(ThemeService themeService, ThemeMapper themeMapper) {
        this.themeService = themeService;
        this.themeMapper = themeMapper;
    }

    @PostMapping
    public ResponseEntity<ThemeResponseDto> saveTheme(@Valid @RequestBody ThemeCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(themeService.saveTheme(themeMapper.toCreateCommand(requestDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }
}
