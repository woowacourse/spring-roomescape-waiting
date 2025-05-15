package roomescape.auth.jwt;

import roomescape.auth.AuthToken;
import roomescape.auth.LoginInfo;
import roomescape.business.model.entity.User;

public interface JwtUtil {

    AuthToken createToken(User user);

    LoginInfo validateAndResolveToken(AuthToken token);
}
