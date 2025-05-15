package roomescape.auth.stub;

import roomescape.auth.service.out.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class StubTokenProvider implements TokenProvider {

    public static final Long STUB_MEMBER_ID = 1L;
    public static final String USER_STUB_TOKEN = "user_stub_token";
    public static final String ADMIN_STUB_TOKEN = "admin_stub_token";

    @Override
    public String issue(Member member) {
        if (member.getRole() == Role.USER) {
            return USER_STUB_TOKEN;
        }
        return ADMIN_STUB_TOKEN;
    }

    @Override
    public Role getRole(String token) {
        if (token.equals(USER_STUB_TOKEN)) {
            return Role.USER;
        }
        return Role.ADMIN;
    }

    @Override
    public Long getMemberId(String token) {
        return STUB_MEMBER_ID;
    }
}
