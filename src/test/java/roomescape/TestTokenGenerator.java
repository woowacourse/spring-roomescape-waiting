package roomescape;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.auth.session.UserSession;
import roomescape.user.domain.User;

@Component
public class TestTokenGenerator {

    @Autowired
    private JwtManager jwtManager;

    public String execute(final User user) {
        final Claims claims = Jwts.claims()
                .add(UserSession.Fields.id, user.getId().getValue())
                .add(UserSession.Fields.name, user.getName().getValue())
                .add(UserSession.Fields.role, user.getRole().name())
                .build();

        return jwtManager.generate(claims, TokenType.ACCESS).getValue();
    }

}
