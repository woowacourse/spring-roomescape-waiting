package roomescape.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.login.authorization.AuthorizationHandler;
import roomescape.member.login.authorization.TokenAuthorizationHandler;

@Controller
@RequestMapping("/logout")
public class LogoutApiController {

    private final AuthorizationHandler<String> authorizationHandler;

    public LogoutApiController(TokenAuthorizationHandler tokenAuthorizationHandler) {
        this.authorizationHandler = tokenAuthorizationHandler;
    }

    @PostMapping
    public ResponseEntity<Void> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        authorizationHandler.deleteCookie(httpServletRequest, httpServletResponse);
        return ResponseEntity.ok().build();
    }
}
