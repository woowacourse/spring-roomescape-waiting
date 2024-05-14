package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.handler.ResponseHandler;
import roomescape.auth.service.AuthService;

@Controller
public class AuthController {

    private final AuthService authService;
    private final RequestHandler requestHandler;
    private final ResponseHandler responseHandler;

    public AuthController(AuthService authService, RequestHandler requestHandler, ResponseHandler responseHandler) {
        this.authService = authService;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(HttpServletResponse response, @RequestBody LoginRequest loginRequest) {
        authService.authenticate(loginRequest);
        responseHandler.set(response, authService.createToken(loginRequest).accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<AuthInfo> checkLogin(HttpServletRequest request) {
        AuthInfo authInfo = authService.fetchByToken(requestHandler.extract(request));
        return ResponseEntity.ok().body(authInfo);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        responseHandler.expire(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ResponseEntity.created(URI.create("/login")).build();
    }
}
