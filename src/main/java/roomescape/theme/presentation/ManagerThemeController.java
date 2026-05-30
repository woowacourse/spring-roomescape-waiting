package roomescape.theme.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.theme.application.ThemeService;
import roomescape.theme.dto.request.ThemeSaveRequest;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.theme.dto.response.ThemeSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager/themes")
@RequiredArgsConstructor
public class ManagerThemeController {
    private final ThemeService themeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ThemeSaveResponse>> save(
            @RequestBody @Valid ThemeSaveRequest body
    ) {
        ThemeSaveResponse response = themeService.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ThemeFindResponse>>> findAll() {
        List<ThemeFindResponse> responses = themeService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive long id
    ) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
