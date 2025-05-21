package roomescape.user.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.aop.RequiredRoles;
import roomescape.user.application.UserFacade;
import roomescape.user.application.dto.UserResponse;
import roomescape.user.domain.UserRole;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequiredRoles(UserRole.ADMIN)
@RequestMapping("/users")
public class UserController {

    private final UserFacade userFacade;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userFacade.getAll());
    }
}
