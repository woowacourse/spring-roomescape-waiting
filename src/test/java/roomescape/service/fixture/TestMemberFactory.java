package roomescape.service.fixture;

import static roomescape.model.Role.ADMIN;
import static roomescape.model.Role.MEMBER;

import roomescape.model.Member;
import roomescape.model.Role;

public class TestMemberFactory {

    public static Member createMember(Long id, String name, Role role, String email, String password) {
        return new Member(id, name, role, email, password);
    }

    public static Member createMember(Long id) {
        return createMember(id, "name", MEMBER, "email@email", "password");
    }

    public static Member createAdmin(Long id) {
        return createMember(id, "name", ADMIN, "email@email", "password");
    }
}
