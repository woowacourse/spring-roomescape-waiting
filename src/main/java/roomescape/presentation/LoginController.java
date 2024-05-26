package roomescape.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.AuthenticationService;
import roomescape.application.dto.MemberResponse;
import roomescape.application.dto.TokenRequest;
import roomescape.application.dto.TokenResponse;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final AuthenticationService authenticationService;

    public LoginController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<Void> login(@Valid @RequestBody TokenRequest request, HttpServletResponse response) {
        TokenResponse token = authenticationService.createToken(request);
        authenticationService.setToken(response, token.accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<MemberResponse> check(HttpServletRequest request) {
        String token = authenticationService.extractToken(request.getCookies());
        MemberResponse memberResponse = authenticationService.findMemberByToken(token);
        return ResponseEntity.ok(memberResponse);
    }
}
