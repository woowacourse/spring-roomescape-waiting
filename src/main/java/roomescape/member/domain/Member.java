package roomescape.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    private Long id;
    private String name;
    private Password password;
    private Role role;

    public static Member register(String name, String password) {
        return new Member(null, name, Password.from(password), Role.MEMBER);
    }

    public static Member load(Long id, String name, String encryptedPassword, Role role) {
        return new Member(id, name, Password.load(encryptedPassword), role);
    }

    public void matchPassword(String inputPassword) {
        password.validateMatches(inputPassword);
    }

}
