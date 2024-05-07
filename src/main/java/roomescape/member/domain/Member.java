package roomescape.member.domain;

import java.util.Objects;

public class Member {

    private final long id;
    private final String name;
    private final String email;
    private final String password;
    private MemberRole role;

    public Member(long id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = MemberRole.valueOf(role);
    }

    public Member(long id, String name, String email, String password) {
        this(id, name, email, password, "USER");
    }

    public Member(String name, String email, String password) {
        this(0, email, password, name, "GUEST");
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = MemberRole.valueOf(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member member)) {
            return false;
        }
        return id == member.id && Objects.equals(email, member.email) && Objects.equals(password, member.password)
                && Objects.equals(name, member.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, name);
    }

    @Override
    public String toString() {
        return "Member{" + "email='" + email + '\'' + ", id=" + id + ", name='" + name + '\'' + '}';
    }

}
