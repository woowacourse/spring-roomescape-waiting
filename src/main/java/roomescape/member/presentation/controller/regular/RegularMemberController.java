package roomescape.member.presentation.controller.regular;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.infrastructure.CookieManager;
import roomescape.member.domain.MemberRole;

@RestController
public class RegularMemberController {

    private static final String TOKEN = "token";

    private final CookieManager cookieManager;

    public RegularMemberController(final CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse httpServletResponse) {
        cookieManager.deleteCookie(httpServletResponse, TOKEN);
        return ResponseEntity.noContent().build();
    }
}
