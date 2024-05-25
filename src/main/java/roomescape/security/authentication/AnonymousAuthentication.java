package roomescape.security.authentication;

import roomescape.security.exception.UnauthorizedException;

public class AnonymousAuthentication implements Authentication {

    @Override
    public long getId() {
        throw new UnauthorizedException();
    }

    @Override
    public String getName() {
        throw new UnauthorizedException();
    }

    @Override
    public boolean isNotAdmin() {
        throw new UnauthorizedException();
    }
}
