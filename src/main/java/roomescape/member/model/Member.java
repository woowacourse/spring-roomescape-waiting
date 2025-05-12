package roomescape.member.model;

import roomescape.global.exception.AlreadyEntityException;

public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member generateNormalMember(String name, String email, String password) {
        return new Member(null, name, email, password, Role.NORMAL);
    }

    public Member toEntity(Long id) {
        if (this.id == null) {
            return new Member(id, name, email, password, role);
        }
        throw new AlreadyEntityException("해당 멤버는 이미 엔티티화 된 상태입니다.");
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
