package roomescape.domain;

import roomescape.domain.enums.Role;
import roomescape.exception.member.MemberFieldRequiredException;

public class Member {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;

    public Member(Long id, String name, String email, String password, Role role) {
        validate(name, email, password, role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member withId(Long id) {
        return new Member(id, name, email, password, role);
    }

    private void validate(String name, String email, String password, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validateRole(role);
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new MemberFieldRequiredException("역할");
        }
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new MemberFieldRequiredException("패스워드");
        }
    }


    private void validateName(String name) {
        if (name == null) {
            throw new MemberFieldRequiredException("이름");
        }
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new MemberFieldRequiredException("이메일");
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
