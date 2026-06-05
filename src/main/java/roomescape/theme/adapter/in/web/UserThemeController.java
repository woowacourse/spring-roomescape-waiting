package roomescape.theme.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.theme.application.port.in.FindThemeUseCase;
import roomescape.theme.application.dto.response.ThemeFindResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class UserThemeController {
    private final FindThemeUseCase findThemeUseCase;

    @GetMapping(params = "date")
    public ResponseEntity<ApiResponse<List<ThemeFindResponse>>> findThemesBySlotDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ThemeFindResponse> responses = findThemeUseCase.findThemesBySlotDate(date);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ThemeFindResponse>>> findByDayAndLimit() {
        List<ThemeFindResponse> responses = findThemeUseCase.findPopularTheme();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }
}
