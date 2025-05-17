package roomescape.presentation.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logout")
public class LogoutController {

    @PostMapping
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("token", "null");
        expiredCookie.setMaxAge(0);
        expiredCookie.setSecure(true);
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");

        response.addCookie(expiredCookie);

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header("Location", "/")
                .build();
    }
}
