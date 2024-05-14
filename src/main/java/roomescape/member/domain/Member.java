package roomescape.member.domain;

import roomescape.global.exception.ViolationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Member {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\d+$");
    private static final int NAME_MAXIMUM_LENGTH = 10;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$");

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    public Member(Long id, Member member) {
        this(id, member.name, member.email, member.password, member.role);
    }

    public Member(Long id, String name, String email, String password, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ViolationException("사용 이름은 비어있을 수 없습니다.");
        }
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (matcher.matches()) {
            throw new ViolationException("사용 이름은 숫자로만 구성될 수 없습니다.");
        }
        if (name.length() > NAME_MAXIMUM_LENGTH) {
            throw new ViolationException("사용자 이름은 10자 이하입니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ViolationException("이메일은 비어있을 수 없습니다.");
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new ViolationException("이메일 형식에 맞지 않습니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ViolationException("비밀번호는 비어있을 수 없습니다.");
        }
    }

    public boolean hasSamePassword(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
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
