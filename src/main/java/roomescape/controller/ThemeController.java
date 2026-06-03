package roomescape.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ThemeResponse;
import roomescape.service.ThemeService;

@RequestMapping("/themes")
@RestController
@Validated
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ThemeResponse> getAllThemes() {
        return themeService.getAllThemes();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/popular")
    public List<ThemeResponse> getPopularThemes(
            @RequestParam(defaultValue = "10") int size) {
        LocalDate today = LocalDate.now();
        return themeService.getPopularThemes(today, size);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/available-times")
    public List<AvailableTimeResponse> getReservationTimes(
            @PathVariable long id,
            @NotBlank(message = "날짜를 입력해주세요.")
            @RequestParam String date) {
        return themeService.getAvailableTimeResponses(id, date);
    }
}
