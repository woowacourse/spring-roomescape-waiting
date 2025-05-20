package roomescape.auth.sign.ui;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.auth.session.UserSession;
import roomescape.auth.session.annotation.SignInUser;
import roomescape.auth.sign.application.SignFacade;
import roomescape.auth.sign.ui.dto.SignInWebRequest;
import roomescape.auth.sign.ui.dto.SignUpWebRequest;
import roomescape.auth.sign.ui.dto.UserSessionResponse;
import roomescape.common.uri.UriFactory;

import java.net.URI;

@Controller
@RequiredArgsConstructor
public class SignController {

    private final SignFacade signFacade;

    @PostMapping("/sign-in")
    public ResponseEntity<Void> signIn(@RequestBody final SignInWebRequest request,
                                       final HttpServletResponse httpServletResponse) {
        signFacade.signIn(request.toServiceRequest(), httpServletResponse::addCookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sign-in/check")
    public ResponseEntity<UserSessionResponse> checkSignIn(@SignInUser final UserSession userSession) {
        return ResponseEntity.ok(
                UserSessionResponse.from(userSession));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Long> create(@RequestBody final SignUpWebRequest request) {
        final Long userId = signFacade.signUp(request.toServiceRequest()).getValue();

        // TODO add UserController
        final URI location = UriFactory.buildPath("/users", String.valueOf(userId));
        return ResponseEntity.created(location).body(userId);
    }
}
