package roomescape.presentation.rest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.AuthenticationService;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.auth.AuthenticationTokenCookie;
import roomescape.presentation.request.LoginRequest;
import roomescape.presentation.response.UserResponse;

@RestController
public class LoginController {

    private final AuthenticationService authenticationService;

    public LoginController(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public void performLogin(
            @RequestBody @Valid final LoginRequest request,
            final HttpServletResponse response
    ) {
        String issuedToken = authenticationService.issueToken(request.email(), request.password());
        AuthenticationTokenCookie tokenCookie = AuthenticationTokenCookie.forResponse(issuedToken);
        response.addCookie(tokenCookie);
    }

    @GetMapping("/login/check")
    public UserResponse getUser(@Authenticated final User user) {
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public void performLogout(final HttpServletResponse response) throws IOException {
        AuthenticationTokenCookie tokenCookieForExpire = AuthenticationTokenCookie.forExpire();
        response.addCookie(tokenCookieForExpire);
        response.sendRedirect("/");
    }
}
