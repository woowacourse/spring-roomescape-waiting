package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationReadService;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationResponse;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {
    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid ThemeRequest themeRequest, @LoginMemberId long memberId) {
        ThemeResponse themeResponse = themeService.create(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id())).body(themeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") long themeId) {
        themeService.deleteById(themeId);
        return ResponseEntity.noContent().build();
    }
}
