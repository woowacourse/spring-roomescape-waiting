package roomescape.member.domain;

import lombok.Getter;

@Getter
public class Member {

    private final Long id;
    private final String name;
    private final Role role;
    private final String email;
    private final String password;

    private Member(Long id, String name, Role role, String email, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public static Member signUpUser(String name, String email, String password) {
        return new Member(null, name, Role.USER, email, password);
    }

    public static Member signUpAdmin(String name, String email, String password) {
        return new Member(null, name, Role.ADMIN, email, password);
    }

    public static Member load(Long id, String name, Role role, String email, String password) {
        return new Member(id, name, role, email, password);
    }
}
