package roomescape.auth.domain;

import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class AuthInfo {
    private final Long id;
    private final String name;
    private final String email;
    private final Role role;

    public AuthInfo(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static AuthInfo from(Member member) {
        return new AuthInfo(member.getId(), member.getName(), member.getEmail(), member.getRole());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthInfo authInfo = (AuthInfo) o;
        return Objects.equals(getId(), authInfo.getId()) && Objects.equals(getName(),
                authInfo.getName()) && Objects.equals(getEmail(), authInfo.getEmail())
                && getRole() == authInfo.getRole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getRole());
    }

    @Override
    public String toString() {
        return "AuthInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
