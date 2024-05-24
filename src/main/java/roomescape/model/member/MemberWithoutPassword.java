package roomescape.model.member;

import java.util.Objects;

public class MemberWithoutPassword {

    private final long id;
    private final MemberName name;
    private final MemberEmail email;
    private final Role role;

    public MemberWithoutPassword(long id, String name, String email, Role role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = new MemberEmail(email);
        this.role = role;
    }

    public static MemberWithoutPassword from(Member member) {
        return new MemberWithoutPassword(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    public boolean isNotAdmin() {
        return role.isNotAdmin();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getEmail();
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
        MemberWithoutPassword that = (MemberWithoutPassword) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role);
    }
}
