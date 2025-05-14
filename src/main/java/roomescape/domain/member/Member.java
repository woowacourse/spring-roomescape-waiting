package roomescape.domain.member;

import java.util.Objects;

public class Member {

    private static final int MAX_NAME_LENGTH = 255;

    private final Long id;
    private final String name;
    private final String email;
    private final Role role;
    private final String password;

    public Member(Long id, String name, String email, Role role, String password) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validateRole(role);

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public static Member createWithoutId(String name, String email, Role role, String password) {
        return new Member(null, name, email, role, password);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 이름은 1글자 이상으로 이루어져야 합니다. ");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 이름은 255자를 초과할 수 없습니다. 이름 길이 : " + name.length());
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 이메일은 1글자 이상으로 이루어져야 합니다. ");
        }

        if (email.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 이메일은 255자를 초과할 수 없습니다. 이메일 길이 : " + email.length());
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 1글자 이상으로 이루어져야 합니다. ");
        }

        if (password.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 255자를 초과할 수 없습니다. 비밀번호 길이 : " + password.length());
        }
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("[ERROR] 역할이 존재해야 합니다. ");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) && Objects.equals(name, member.name) && Objects.equals(email, member.email) && Objects.equals(role, member.role) && Objects.equals(password, member.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role, password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}
