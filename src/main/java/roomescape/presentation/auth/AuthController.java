package roomescape.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.auth.AuthService;
import roomescape.application.auth.request.LoginRequest;
import roomescape.application.auth.request.SignupRequest;
import roomescape.application.auth.response.LoginResponse;
import roomescape.common.auth.SessionKeys;
import roomescape.domain.user.User;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {
        User user = authService.login(request);
        return loginResponse(session, user);
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpSession session
    ) {
        User user = authService.signup(request);
        session.setAttribute(SessionKeys.LOGIN_USER, user);
        return ResponseEntity.created(URI.create("/users/" + user.getId()))
                .body(LoginResponse.from(user));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private ResponseEntity<LoginResponse> loginResponse(HttpSession session, User user) {
        session.setAttribute(SessionKeys.LOGIN_USER, user);
        return ResponseEntity.ok(LoginResponse.from(user));
    }
}
