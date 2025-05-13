package roomescape.entity;

import roomescape.exception.InvalidMemberException;

public class LoginMember {
    private final Long id;
    private final String name;
    private final Role role;

    public LoginMember(Long id, String name, Role role) {
        validate(id, name, role);
        this.id = id;
        this.name = name;
        this.role = role;
    }

    private void validate(Long id, String name, Role role) {
        if (id == null) {
            throw new InvalidMemberException("로그인 회원 id는 비어있을 수 없습니다.");
        }
        if (name == null) {
            throw new InvalidMemberException("로그인 회원 이름은 비어있을 수 없습니다.");
        }
        if (role == null) {
            throw new InvalidMemberException("로그인 회원 역할 비어있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
