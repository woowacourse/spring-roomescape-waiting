package roomescape.presentation.rest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.AuthenticationService;
import roomescape.application.UserService;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.auth.AuthenticationTokenCookie;
import roomescape.presentation.request.LoginRequest;
import roomescape.presentation.response.UserResponse;

@RestController
public class LoginController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public LoginController(
        final AuthenticationService authenticationService,
        final UserService userService
    ) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public void performLogin(
            @RequestBody @Valid final LoginRequest request,
            final HttpServletResponse response
    ) {
        var issuedToken = authenticationService.issueToken(request.email(), request.password());
        var tokenCookie = AuthenticationTokenCookie.forResponse(issuedToken);
        response.addCookie(tokenCookie);
    }

    @GetMapping("/login/check")
    public UserResponse getUser(@Authenticated final AuthenticationInfo authenticationInfo) {
        var user = userService.getById(authenticationInfo.id());
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public void performLogout(final HttpServletResponse response) throws IOException {
        var tokenCookieForExpire = AuthenticationTokenCookie.forExpire();
        response.addCookie(tokenCookieForExpire);
        response.sendRedirect("/");
    }
}
