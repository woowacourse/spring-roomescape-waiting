package roomescape.service.dto;

import roomescape.model.member.Role;

import java.util.Objects;

public class MemberInfo {

    private final long id;
    private final String name;
    private final String email;
    private final Role role;

    public MemberInfo(long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public boolean isNotAdmin() {
        return role.isNotAdmin();
    }

    public long getId() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberInfo that = (MemberInfo) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(email, that.email) && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role);
    }
}
