package roomescape.domain;

import roomescape.exception.BadRequestException;

public class Member {

    private static final String EMAIL_PATTERN = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    public Member(Long id, String name, String email, String password, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validateRole(role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new BadRequestException("역할에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("이름에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일에 빈값을 입력할 수 없습니다.");
        }

        if (!email.matches(EMAIL_PATTERN)) {
            throw new BadRequestException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("비밀번호에 빈값을 입력할 수 없습니다.");
        }
    }

    public Member setId(Long id) {
        return new Member(id, name, email, password, role);
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
