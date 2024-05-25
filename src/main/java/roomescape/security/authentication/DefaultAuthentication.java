package roomescape.security.authentication;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public class DefaultAuthentication implements Authentication {
    private final long id;
    private final String name;
    private final Role role;

    private DefaultAuthentication(long id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public static Authentication from(Member member) {
        return new DefaultAuthentication(member.getId(), member.getName(), member.getRole());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isNotAdmin() {
        return !role.isAdmin();
    }
}
