package roomescape.theme.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.aop.RequiredRoles;
import roomescape.theme.application.ThemeFacade;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.user.domain.UserRole;

import java.util.List;

@RestController
@RequestMapping(ThemeController.BASE_PATH)
@RequiredRoles(UserRole.NORMAL)
@RequiredArgsConstructor
public class ThemeController {

    public static final String BASE_PATH = "/themes";

    private final ThemeFacade themeFacade;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAll() {
        return ResponseEntity.ok(themeFacade.getAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ThemeResponse>> getRanking() {
        return ResponseEntity.ok(themeFacade.getRanking());
    }
}
