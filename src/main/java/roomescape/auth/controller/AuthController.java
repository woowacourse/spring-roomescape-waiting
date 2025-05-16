package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.MemberLoginCheckResponse;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.handler.AuthorizationHandler;
import roomescape.auth.infrastructure.methodargument.AuthorizedMember;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.auth.service.AuthServiceFacade;

@RestController
public class AuthController {
    private final AuthServiceFacade authService;
    private final AuthorizationHandler authorizationHandler;

    public AuthController(AuthServiceFacade authService, AuthorizationHandler authorizationHandler) {
        this.authService = authService;
        this.authorizationHandler = authorizationHandler;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(HttpServletResponse response, @RequestBody @Valid LoginRequest request) {
        AuthorizationPrincipal principal = authService.login(request);
        authorizationHandler.setPrincipal(response, principal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberLoginCheckResponse> loginCheck(@AuthorizedMember MemberPrincipal memberPrincipal) {
        authService.validateMemberExistence(memberPrincipal);
        MemberLoginCheckResponse response = MemberLoginCheckResponse.fromMemberPrincipal(memberPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authorizationHandler.removePrincipal(response);
        return ResponseEntity.ok().build();
    }
}
