package roomescape.theme.ui;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.response.ApiResponse;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeResponse;

@RestController
@AllArgsConstructor
@RequestMapping("themes")
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getAll() {
        List<ThemeResponse> response = themeService.findAll();
        ApiResponse<List<ThemeResponse>> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/ranked")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getRankedByPeriod() {
        List<ThemeResponse> response = themeService.findRankedByPeriod();
        ApiResponse<List<ThemeResponse>> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }
}
