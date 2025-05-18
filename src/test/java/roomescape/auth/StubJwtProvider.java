package roomescape.auth;

import roomescape.member.MemberRole;

public class StubJwtProvider extends JwtProvider {

    private String returnValue;

    @Override
    public String provideToken(final String email, final MemberRole role, final String name) {
        return returnValue;
    }

    public void stubToken(final String returnValue) {
        this.returnValue = returnValue;
    }
}
