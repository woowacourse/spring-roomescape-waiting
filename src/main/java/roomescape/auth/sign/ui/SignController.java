package roomescape.auth.sign.ui;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.auth.session.Session;
import roomescape.auth.session.annotation.UserSession;
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
    public ResponseEntity<Void> signIn(@RequestBody final SignInWebRequest signInWebRequest,
                                       final HttpServletResponse response) {
        signFacade.signIn(signInWebRequest, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sign-in/check")
    public ResponseEntity<UserSessionResponse> checkSignIn(@UserSession final Session session) {
        return ResponseEntity.ok(
                UserSessionResponse.from(session));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserSessionResponse> create(@RequestBody final SignUpWebRequest request) {
        final UserSessionResponse response = signFacade.signUp(request);

        // TODO add UserController
        final URI location = UriFactory.buildPath("/users", String.valueOf(response.userId()));
        return ResponseEntity.created(location)
                .body(response);
    }
}
