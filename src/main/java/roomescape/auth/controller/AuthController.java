package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.MemberResponse;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.handler.ResponseHandler;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.dto.SignUpCommand;

@RestController
public class AuthController {

    private final AuthService authService;

    private final RequestHandler requestHandler;

    private final ResponseHandler responseHandler;

    public AuthController(AuthService authService, RequestHandler requestHandler, ResponseHandler responseHandler) {
        this.authService = authService;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
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

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        SignUpCommand signUpCommand = new SignUpCommand(
                signUpRequest.name(),
                signUpRequest.email(),
                signUpRequest.password()
        );
        MemberResponse memberResponse = authService.signUp(signUpCommand);
        return ResponseEntity.created(URI.create("/login")).body(memberResponse);
    }
}
