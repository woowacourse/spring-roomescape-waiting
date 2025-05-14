package roomescape.presentation.rest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.application.AuthenticationService;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.auth.AuthenticationTokenCookie;
import roomescape.presentation.request.LoginRequest;
import roomescape.presentation.response.UserResponse;

@Controller
public class LoginController {

    private final AuthenticationService authenticationService;

    public LoginController(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> performLogin(
            @RequestBody @Valid final LoginRequest request,
            final HttpServletResponse response
    ) {
        var issuedToken = authenticationService.issueToken(request.email(), request.password());
        var tokenCookie = AuthenticationTokenCookie.forResponse(issuedToken);
        response.addCookie(tokenCookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<UserResponse> getUser(@Authenticated final User user) {
        var response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public void performLogout(final HttpServletResponse response) throws IOException {
        var tokenCookieForExpire = AuthenticationTokenCookie.forExpire();
        response.addCookie(tokenCookieForExpire);
        response.sendRedirect("/");
    }
}
