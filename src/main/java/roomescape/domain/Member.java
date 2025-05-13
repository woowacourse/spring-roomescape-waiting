package roomescape.domain;

import roomescape.exception.InvalidMemberException;

public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(Long id, String name, String email, String password, Role role) {
        validate(name, email, password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    private void validate(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new InvalidMemberException("이름은 비어있을 수 없습니다");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidMemberException("이메일은 비어있을 수 없습니다");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidMemberException("비밀번호는 비어있을 수 없습니다");
        }
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

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
